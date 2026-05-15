import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import { CheckCircle, XCircle, LogOut, AlertTriangle, Users, ShieldCheck, Clock } from 'lucide-react';

const Sidebar = ({ user, logout }) => (
  <aside className="sidebar">
    <div className="sidebar-brand">
      <div className="sidebar-brand-icon">🛡️</div>
      <div>
        <div className="sidebar-brand-text">E-Gatepass</div>
        <div className="sidebar-brand-sub">Warden Panel</div>
      </div>
    </div>
    <nav className="sidebar-nav">
      <span className="sidebar-section-label">Dashboard</span>
      <button className="sidebar-item active"><ShieldCheck size={16} /> Pending Approvals</button>
    </nav>
    <div className="sidebar-footer">
      <div className="user-chip" style={{ marginBottom: '0.5rem' }}>
        <div className="user-avatar">{user?.name?.[0]?.toUpperCase()}</div>
        <div className="user-info">
          <div className="user-name">{user?.name}</div>
          <div className="user-role">Warden</div>
        </div>
      </div>
      <button className="sidebar-item" onClick={logout} style={{ width: '100%' }}>
        <LogOut size={16} /> Sign Out
      </button>
    </div>
  </aside>
);

const WardenDashboard = () => {
  const { user, logout } = useAuth();
  const [pending, setPending] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedIds, setSelectedIds] = useState([]);

  useEffect(() => { fetchData(); }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [pendingRes, statsRes] = await Promise.all([api.get('/warden/pending'), api.get('/warden/stats')]);
      setPending(pendingRes.data);
      setStats(statsRes.data);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleReview = async (id, approved) => {
    try {
      await api.post(`/warden/gatepass/${id}/review`, { approved, remarks: approved ? 'Approved by Warden' : 'Rejected by Warden' });
      fetchData();
    } catch (err) { console.error(err); }
  };

  const handleBulkApprove = async () => {
    if (!selectedIds.length) return;
    try {
      await api.post('/warden/bulk-approve', { ids: selectedIds });
      setSelectedIds([]); fetchData();
    } catch (err) { console.error(err); }
  };

  const toggleSelect = (id) => setSelectedIds(p => p.includes(id) ? p.filter(x => x !== id) : [...p, id]);
  const allSelected = selectedIds.length === pending.length && pending.length > 0;

  return (
    <div className="app-layout">
      <Sidebar user={user} logout={logout} />
      <main className="page-content animate-fade-in">
        <div className="page-header">
          <div>
            <h1 className="page-title">Warden Dashboard</h1>
            <p className="page-subtitle">{user?.designation || 'Hostel Warden'}</p>
          </div>
          {selectedIds.length > 0 && (
            <button className="btn btn-primary" onClick={handleBulkApprove}>
              <CheckCircle size={16} /> Approve Selected ({selectedIds.length})
            </button>
          )}
        </div>

        {stats && (
          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-icon blue"><Users size={20} /></div>
              <div className="stat-value">{stats.currentlyOutside}</div>
              <div className="stat-label">Currently Outside</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon red"><AlertTriangle size={20} /></div>
              <div className="stat-value" style={{ color: 'var(--danger)' }}>{stats.overdue}</div>
              <div className="stat-label">Overdue Students</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon yellow"><Clock size={20} /></div>
              <div className="stat-value" style={{ color: 'var(--warning)' }}>{pending.length}</div>
              <div className="stat-label">Awaiting Approval</div>
            </div>
          </div>
        )}

        <div className="card">
          <div className="card-header">
            <span className="card-title">Pending Final Approvals</span>
            <span className="badge badge-warning" style={{ background: 'var(--warning-bg)', color: 'var(--warning)', border: '1px solid rgba(245,158,11,0.2)' }}>{pending.length}</span>
          </div>

          {loading ? (
            <div className="empty-state"><div className="empty-text">Loading...</div></div>
          ) : pending.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">✅</div>
              <div className="empty-text">No pending approvals. All clear!</div>
            </div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th><input type="checkbox" checked={allSelected} onChange={e => setSelectedIds(e.target.checked ? pending.map(p => p.id) : [])} /></th>
                    <th>Student</th>
                    <th>Destination</th>
                    <th>Timeline</th>
                    <th>Coord. Remark</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {pending.map(gp => (
                    <tr key={gp.id}>
                      <td><input type="checkbox" checked={selectedIds.includes(gp.id)} onChange={() => toggleSelect(gp.id)} /></td>
                      <td>
                        <strong>{gp.studentName}</strong>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '2px' }}>Room: {gp.studentRoom}</div>
                      </td>
                      <td>{gp.destination}</td>
                      <td style={{ fontSize: '0.775rem' }}>
                        <div style={{ color: 'var(--text-muted)' }}>Out: {new Date(gp.outDateTime).toLocaleString()}</div>
                        <div style={{ color: 'var(--text-muted)' }}>In: {new Date(gp.expectedReturnDateTime).toLocaleString()}</div>
                      </td>
                      <td style={{ color: 'var(--text-muted)', fontSize: '0.775rem' }}>{gp.coordinatorRemarks || 'Approved'}</td>
                      <td>
                        <div className="flex gap-2">
                          <button className="btn btn-success btn-sm" onClick={() => handleReview(gp.id, true)}><CheckCircle size={15} /></button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleReview(gp.id, false)}><XCircle size={15} /></button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default WardenDashboard;
