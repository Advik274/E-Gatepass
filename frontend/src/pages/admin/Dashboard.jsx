import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import { LogOut, Activity, Users, ShieldAlert, Key, Settings, ToggleLeft, ToggleRight } from 'lucide-react';

const Sidebar = ({ user, logout, tab, setTab }) => (
  <aside className="sidebar">
    <div className="sidebar-brand">
      <div className="sidebar-brand-icon">🛡️</div>
      <div>
        <div className="sidebar-brand-text">E-Gatepass</div>
        <div className="sidebar-brand-sub">Admin Panel</div>
      </div>
    </div>
    <nav className="sidebar-nav">
      <span className="sidebar-section-label">Administration</span>
      <button className={`sidebar-item ${tab === 'overview' ? 'active' : ''}`} onClick={() => setTab('overview')}><Activity size={16} /> Overview</button>
      <button className={`sidebar-item ${tab === 'users' ? 'active' : ''}`} onClick={() => setTab('users')}><Users size={16} /> Users</button>
    </nav>
    <div className="sidebar-footer">
      <div className="user-chip" style={{ marginBottom: '0.5rem' }}>
        <div className="user-avatar">{user?.name?.[0]?.toUpperCase()}</div>
        <div className="user-info">
          <div className="user-name">{user?.name}</div>
          <div className="user-role">Admin</div>
        </div>
      </div>
      <button className="sidebar-item" onClick={logout} style={{ width: '100%' }}><LogOut size={16} /> Sign Out</button>
    </div>
  </aside>
);

const AdminDashboard = () => {
  const { user, logout } = useAuth();
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('overview');

  useEffect(() => { fetchData(); }, []);

  const fetchData = async () => {
    try {
      const [statsRes, usersRes] = await Promise.all([api.get('/admin/stats'), api.get('/admin/users')]);
      setStats(statsRes.data); setUsers(usersRes.data);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleToggle = async (id) => {
    try { await api.patch(`/admin/users/${id}/toggle-status`); fetchData(); }
    catch (err) { console.error(err); }
  };

  const roleColors = { STUDENT: 'badge-cyan', COORDINATOR: 'badge-primary', WARDEN: 'badge-pending', SECURITY: 'badge-success', ADMIN: 'badge-danger' };

  return (
    <div className="app-layout">
      <Sidebar user={user} logout={logout} tab={tab} setTab={setTab} />
      <main className="page-content animate-fade-in">
        <div className="page-header">
          <div>
            <h1 className="page-title">{tab === 'overview' ? 'System Overview' : 'User Management'}</h1>
            <p className="page-subtitle">Full administrative control</p>
          </div>
        </div>

        {tab === 'overview' && (
          loading ? (
            <div className="empty-state"><div className="empty-text">Loading...</div></div>
          ) : !stats ? (
            <div className="empty-state"><div className="empty-text">Failed to load stats. Please refresh.</div></div>
          ) : (
          <div className="animate-fade-in">
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-icon blue"><Activity size={20} /></div>
                <div className="stat-value">{stats.totalGatepasses}</div>
                <div className="stat-label">Total Gatepasses</div>
              </div>
              <div className="stat-card">
                <div className="stat-icon cyan"><Key size={20} /></div>
                <div className="stat-value" style={{ color: 'var(--accent)' }}>{stats.active}</div>
                <div className="stat-label">Currently Active</div>
              </div>
              <div className="stat-card">
                <div className="stat-icon red"><ShieldAlert size={20} /></div>
                <div className="stat-value" style={{ color: 'var(--danger)' }}>{stats.overdue}</div>
                <div className="stat-label">Overdue</div>
              </div>
              <div className="stat-card">
                <div className="stat-icon green"><Users size={20} /></div>
                <div className="stat-value">{users.length}</div>
                <div className="stat-label">Total Users</div>
              </div>
            </div>
            <div className="card">
              <div className="card-header"><span className="card-title">System Health</span><span className="badge badge-success">● Operational</span></div>
              <p style={{ fontSize: '0.825rem', color: 'var(--text-muted)' }}>All services are running normally. No alerts or issues detected.</p>
            </div>
          </div>
        ))}

        {tab === 'users' && (
          <div className="card animate-fade-in">
            <div className="card-header">
              <span className="card-title">All Users</span>
              <span className="badge badge-primary">{users.length} total</span>
            </div>
            {loading ? (
              <div className="empty-state"><div className="empty-text">Loading...</div></div>
            ) : (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Role</th>
                      <th>Email</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map(u => (
                      <tr key={u.id}>
                        <td><strong>{u.name}</strong></td>
                        <td><span className={`badge ${roleColors[u.role] || 'badge-primary'}`}>{u.role}</span></td>
                        <td style={{ fontFamily: 'var(--font-mono)', fontSize: '0.775rem' }}>{u.email}</td>
                        <td>{u.active ? <span className="badge badge-success">Active</span> : <span className="badge badge-danger">Inactive</span>}</td>
                        <td>
                          <button className="btn btn-secondary btn-sm" onClick={() => handleToggle(u.id)} style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                            {u.active ? <ToggleRight size={14} /> : <ToggleLeft size={14} />}
                            {u.active ? 'Disable' : 'Enable'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
};

export default AdminDashboard;
