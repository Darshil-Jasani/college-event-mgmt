let auth = null;      // "Basic xxx"
let roles = "";       // username acts as role hint in this demo

const $ = id => document.getElementById(id);
const esc = s => String(s ?? "").replace(/[&<>"']/g, c => ({ "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;" }[c]));

function say(text, ok = true) {
  $("msg").textContent = text;
  $("msg").className = ok ? "ok" : "err";
}

async function api(path, method = "GET", body) {
  const res = await fetch(path, {
    method,
    headers: { "Authorization": auth, "Content-Type": "application/json" },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    let err = res.statusText;
    try { err = (await res.json()).error || err; } catch (_) {}
    throw new Error(res.status === 403 ? "Not allowed for your role" : err);
  }
  return res.status === 204 ? null : res.json();
}

async function load() {
  try {
    const events = await api("/api/events");
    const staff = ["admin", "organizer"].includes(roles);
    $("createBox").hidden = !staff;
    $("events").innerHTML = events.map(e => `
      <div class="card">
        <h3>${esc(e.title)}</h3>
        <div class="muted">📍 ${esc(e.venue)} · 🗓 ${esc(e.eventDate.replace("T", " "))}</div>
        <p>${esc(e.description)}</p>
        <div class="muted">Capacity: ${e.capacity}</div>
        <div class="actions">
          <button data-reg="${e.id}">Register</button>
          <button class="secondary" data-cancel="${e.id}">Cancel</button>
          ${staff ? `<button class="secondary" data-list="${e.id}">Attendees</button>
                     <button class="danger" data-del="${e.id}">Delete</button>` : ""}
        </div>
        <div id="att-${e.id}" class="muted"></div>
      </div>`).join("") || "<p class='muted'>No events yet.</p>";
  } catch (e) { say(e.message, false); }
}

$("loginBtn").onclick = async () => {
  auth = "Basic " + btoa($("user").value + ":" + $("pass").value);
  roles = $("user").value;
  try {
    await api("/api/events");
    $("who").textContent = "Signed in as " + roles;
    say("Signed in", true);
    load();
  } catch (e) { auth = null; say("Login failed", false); }
};

$("createBtn").onclick = async () => {
  try {
    await api("/api/events", "POST", {
      title: $("title").value, venue: $("venue").value, description: $("desc").value,
      eventDate: $("date").value + ":00", capacity: Number($("capacity").value),
    });
    say("Event created"); load();
  } catch (e) { say(e.message, false); }
};

$("events").onclick = async ev => {
  const d = ev.target.dataset;
  try {
    if (d.reg) { await api(`/api/events/${d.reg}/register`, "POST"); say("Registered!"); }
    if (d.cancel) { await api(`/api/events/${d.cancel}/register`, "DELETE"); say("Registration cancelled"); }
    if (d.del) { await api(`/api/events/${d.del}`, "DELETE"); say("Event deleted"); load(); }
    if (d.list) {
      const regs = await api(`/api/events/${d.list}/registrations`);
      $("att-" + d.list).innerHTML = regs.map(r =>
        `${esc(r.username)} — ${r.status}${r.attended ? " ✅" : ""}
         <button class="secondary" data-att="${d.list}|${esc(r.username)}">Mark present</button>`).join("<br>") || "No registrations";
    }
    if (d.att) {
      const [id, user] = d.att.split("|");
      await api(`/api/events/${id}/attendance/${encodeURIComponent(user)}?attended=true`, "POST");
      say("Attendance marked");
    }
  } catch (e) { say(e.message, false); }
};
