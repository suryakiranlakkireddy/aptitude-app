// Shared fetch wrapper for the admin panel. Talks to the same Spring Boot backend as the Flutter app.
const API_BASE = window.localStorage.getItem('api_base') || 'https://aptitude-app-backend-n7vq.onrender.com/api';

async function apiRequest(path, { method = 'GET', body } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  const token = localStorage.getItem('admin_token');
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (res.status === 401 || res.status === 403) {
    localStorage.removeItem('admin_token');
    window.location.href = 'index.html';
    return;
  }

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) throw new Error(data?.error || 'Request failed');
  return data;
}

function requireAdmin() {
  const token = localStorage.getItem('admin_token');
  const role = localStorage.getItem('admin_role');
  if (!token || role !== 'ADMIN') {
    window.location.href = 'index.html';
  }
}

function logoutAdmin() {
  localStorage.removeItem('admin_token');
  localStorage.removeItem('admin_role');
  window.location.href = 'index.html';
}
