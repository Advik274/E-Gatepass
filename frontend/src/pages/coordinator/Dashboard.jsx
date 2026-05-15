import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import { CheckCircle, XCircle, LogOut, ClipboardList } from 'lucide-react';

const Sidebar = ({ user, logout }) => (
  <aside className="sidebar">
    <div className="sidebar-brand">
      <div className="sidebar-brand-icon">🛡️</div>
      <div>
        <div className="sidebar-brand-text">E-Gatepass</div>
        <div className="sidebar-brand-sub">Coordinator</div>
      </div>
    </div>
    <nav className="sidebar-nav">
      <span className="sidebar-section-label">Dashboard</span>
      <button className="sidebar-item active"><ClipboardList size={16} /> Pending Requests</button>
    </nav>
    <div className="sidebar-footer">
      <div className="user-chip" style={{ marginBottom: '0.5rem' }}>
        <div className="user-avatar">{user?.name?.[0]?.toUpperCase()}</div>
        <div className="user-info">
          <div className="user-name">{user?.name}</div>
          <div className="user-role">Coordinator</div>
        </div>
      </div>
      <button className="sidebar-item" onClick={logout} style={{ width: '100%' }}><LogOut size={16} /> Sign Out</button>
    </div>
  </aside>
);

const CoordinatorDashboard = () => {
  const { user, logout } = useAuth();
  const [pending, setPending] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedIds, setSelectedIds] = useState([]);

  useEffect(() => { fetchPending(); }, []);

  const fetchPending = async () => {
    try {
      const res = await api.get('/coordinator/pending');
      setPending(res.data);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleReview = async (id, approved) => {
    try {
      await api.post(`/coordinator/gatepass/${id}/review`, { approved, remarks: approved ? 'Approved by Coordinator' : 'Rejected by Coordinator' });
      fetchPending();
    } catch (err) { alert('Action failed'); }
  };

  const handleBulkApprove = async () => {
    if (!selectedIds.length) return;
    try {
      await api.post('/coordinator/bulk-approve', { ids: selectedIds });
      setSelectedIds([]); fetchPending();
    } catch (err) { alert('Bulk action failed'); }
  };

  const toggleSelect = (id) => setSelectedIds(p => p.includes(id) ? p.filter(x => x !== id) : [...p, id]);
  const allSelected = selectedIds.length === pending.length && pending.length > 0;

  return (
    <div className="app-layout">
      <Sidebar user={user} logout={logout} />
      <main className="page-content animate-fade-in">
        <div className="page-header">
          <div>
            <h1 className="page-title">Coordinator Dashboard</h1>
            <p className="page-subtitle">{user?.designation || 'Floor Coordinator'}</p>
          </div>
          {selectedIds.length > 0 && (
            <button className="btn btn-primary" onClick={handleBulkApprove}>
              <CheckCircle size={16} /> Approve Selected ({selectedIds.length})
            </button>
          )}
        </div>

        <div className="card">
          <div className="card-header">
            <span className="card-title">Pending Requests</span>
            <span className="badge badge-pending">{pending.length} pending</span>
          </div>

          {loading ? (
            <div className="empty-state"><div className="empty-text">Loading...</div></div>
          ) : pending.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">✅</div>
              <div className="empty-text">No pending requests at the moment.</div>
            </div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th><input type="checkbox" checked={allSelected} onChange={e => setSelectedIds(e.target.checked ? pending.map(p => p.id) : [])} /></th>
                    <th>Student</th>
                    <th>Destination</th>
                    <th>Out / Return</th>
                    <th>Reason</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {pending.map(gp => (
                    <tr key={gp.id}>
                      <td><input type="checkbox" checked={selectedIds.includes(gp.id)} onChange={() => toggleSelect(gp.id)} /></td>
                      <td>
                        <strong>{gp.studentName}</strong>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '2px' }}>{gp.studentRollNumber}</div>
                        {gp.urgent && <span className="badge badge-danger" style={{ marginTop: '4px' }}>URGENT</span>}
                      </td>
                      <td>{gp.destination}</td>
                      <td style={{ fontSize: '0.775rem' }}>
                        <div style={{ color: 'var(--text-muted)' }}>Out: {new Date(gp.outDateTime).toLocaleString()}</div>
                        <div style={{ color: 'var(--text-muted)' }}>In: {new Date(gp.expectedReturnDateTime).toLocaleString()}</div>
                      </td>
                      <td style={{ maxWidth: 180, fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{gp.reason}</td>
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

export default CoordinatorDashboard;
