"""Validate the four bundled baseline instrumentation archives from their original bytes."""
import hashlib
import json
from pathlib import Path
import xml.etree.ElementTree as ET
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
EXPECTED_SET = "090ea7c391f23023283d984543afc148c299700ca784095f38a7aa52747cfcae"

def require(test, message):
    if not test:
        raise ValueError(message)

def read_lane(record):
    path = ROOT / record["bundle_path"]
    digest = hashlib.sha256(path.read_bytes()).hexdigest()
    require(digest == record["digest"].removeprefix("sha256:"), f"{path.name} has a different digest")
    with ZipFile(path) as receipt:
        require(receipt.testzip() is None, "ZIP integrity failure")
        paths = receipt.namelist()
        xml_path, = [p for p in paths if p.endswith(".xml") and "/TEST-" in p]
        exit_path, = [p for p in paths if p.endswith("test-result-exit-code.txt")]
        require(receipt.read(exit_path).strip() == b"0", "Instrumentation failed")
        root = ET.fromstring(receipt.read(xml_path))
        for name in ("failures", "errors", "skipped"):
            require(root.get(name) == "0", f"Nonzero {name}")
        cases = root.findall(".//testcase")
        names = sorted({f"{c.attrib['classname']}#{c.attrib['name']}" for c in cases})
        require(len(cases) == len(names) == int(root.attrib["tests"]) == 135, "Unexpected case count")
        require(not any(c.find(s) is not None for c in cases for s in ("failure","error","skipped")), "Non-pass case")
        case_hash = hashlib.sha256("\n".join(names).encode()).hexdigest()
        require(case_hash == EXPECTED_SET, "Case identities changed")
        expected_logs = {"logcat-" + n.replace("#", "-") + ".txt" for n in names}
        actual_logs = {p.rsplit("/",1)[-1] for p in paths if "/logcat-" in p}
        require(actual_logs == expected_logs, "Per-case log identities differ")
        print(f"PASS {record['run']} | {xml_path.rsplit('/',1)[-1]} | 135 cases and logs | {digest}")
    return names

if __name__ == "__main__":
    records = json.loads((ROOT / "evidence" / "final_artifacts.json").read_text())
    lanes = [r for r in records if "bundle_path" in r]
    require(len(lanes) == 4, "Expected four lanes")
    for lane in lanes:
        read_lane(lane)
