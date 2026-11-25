// Read-only extraction of the pinned predecessor visible-context checkpoint.
// The caller supplies existing private source bytes. This module writes no files.
// Render only the returned entries; do not expose internal records/tool payloads.
import { createHash } from "node:crypto";

export function extractVisibleContext(sourceBytes) {
  const prefixBytes = 34283512;
  const prefixHash = "ec0f036c4c8072e2e2452fc49351227c18be52bb97b8265190b99bb8d5f9baf0";
  const canonicalHash = "17f00fb5b01d08c78267256ee4a64b52f5d1be435362f6a8951773054ddc3c3f";
  const sha256 = bytes => createHash("sha256").update(bytes).digest("hex");
  const file = Buffer.from(sourceBytes);
  if (file.length < prefixBytes) throw new Error("Source shorter than checkpoint");
  const prefix = file.subarray(0, prefixBytes);
  if (prefix.at(-1) !== 10 || sha256(prefix) !== prefixHash)
    throw new Error("Complete-line source-prefix integrity mismatch");
  const lines = prefix.toString("utf8").split("\n").filter(Boolean);
  if (lines.length !== 6832) throw new Error("Source line count mismatch");
  const records = lines.map((line, i) => ({ line: i + 1, data: JSON.parse(line) }));
  const messages = records.filter(r =>
    r.data.type === "response_item" &&
    r.data.payload.type === "message" &&
    ["user", "assistant"].includes(r.data.payload.role));
  const visibleMessages = messages.filter(r => !(
    r.data.payload.role === "user" &&
    r.data.payload.content.every(c =>
      (c.text || "").trimStart().startsWith("<environment_context>"))));
  const directVisible = visibleMessages.map(r => ({
    line: r.line, timestamp: r.data.timestamp, id: r.data.payload.id,
    role: r.data.payload.role, phase: r.data.payload.phase || null,
    text: r.data.payload.content.map(c => c.text || "").join("")
  }));
  const directAssistantIds = new Set(messages
    .filter(r => r.data.payload.role === "assistant").map(r => r.data.payload.id));
  const agentEvents = records.filter(r =>
    r.data.type === "event_msg" &&
    r.data.payload.type === "item_completed" &&
    r.data.payload.item.type === "AgentMessage");
  const questionCards = agentEvents
    .filter(r => !directAssistantIds.has(r.data.payload.item.id))
    .map(r => ({
      line: r.line, timestamp: r.data.timestamp, id: r.data.payload.item.id,
      role: "assistant", phase: "async_question",
      text: r.data.payload.item.content.map(c => c.text || "").join("")
    }));
  if (questionCards.length !== 2 || questionCards[0].line !== 220 ||
      questionCards[1].line !== 2130)
    throw new Error("Unexpected async-question coverage");
  const delegated = records[12];
  if (delegated.line !== 13 || delegated.data.type !== "response_item" ||
      delegated.data.payload.type !== "function_call_output" ||
      delegated.data.payload.namespace !== "codex_app" ||
      delegated.data.payload.name !== "create_thread" ||
      !delegated.data.payload.output.startsWith("<codex_delegation>"))
    throw new Error("Initial handoff identity mismatch");
  const entries = [...directVisible, ...questionCards, {
    line: 13, timestamp: delegated.data.timestamp, id: delegated.data.payload.id,
    role: "delegated_handoff", phase: null, text: delegated.data.payload.output
  }].sort((a, b) => a.line - b.line);
  const canonical = Buffer.from(JSON.stringify(entries), "utf8");
  if (entries.length !== 145 || canonical.length !== 104999 ||
      sha256(canonical) !== canonicalHash)
    throw new Error("Canonical visible-context integrity mismatch");
  return {
    prefixBytes, prefixHash, canonicalBytes: canonical.length, canonicalHash,
    entryCount: entries.length, entries
  };
}
