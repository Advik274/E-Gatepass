import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import { PlusCircle, Clock, MapPin, LogOut, FileText, X, AlertTriangle } from 'lucide-react';

const Sidebar = ({ user, logout }) => (
  <aside className="sidebar">
    <div className="sidebar-brand">
      <div className="sidebar-brand-icon">🛡️</div>
      <div>
        <div className="sidebar-brand-text">E-Gatepass</div>
        <div className="sidebar-brand-sub">Hostel System</div>
      </div>
    </div>
    <nav className="sidebar-nav">
      <span className="sidebar-section-label">Navigation</span>
      <button className="sidebar-item active">
        <FileText size={16} /> My Gatepasses
      </button>
    </nav>
    <div className="sidebar-footer">
      <div className="user-chip" style={{ marginBottom: '0.5rem' }}>
        <div className="user-avatar">{user?.name?.[0]?.toUpperCase()}</div>
        <div className="user-info">
          <div className="user-name">{user?.name}</div>
          <div className="user-role">Student</div>
        </div>
      </div>
      <button className="sidebar-item" onClick={logout} style={{ width: '100%' }}>
        <LogOut size={16} /> Sign Out
      </button>
    </div>
  </aside>
);

const getStatusClass = (status) => {
  const map = {
    PENDING: 'badge-pending', COORDINATOR_APPROVED: 'badge-primary',
    COORDINATOR_REJECTED: 'badge-danger', WARDEN_APPROVED: 'badge-cyan',
    WARDEN_REJECTED: 'badge-danger', ACTIVE: 'badge-primary',
    COMPLETED: 'badge-success', OVERDUE: 'badge-danger',
    REJECTED: 'badge-danger', CANCELLED: 'badge-danger'
  };
  return `badge ${map[status] || 'badge-pending'}`;
};

const StudentDashboard = () => {
  const { user, logout } = useAuth();
  const [gatepasses, setGatepasses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    destination: '', reason: '', outDateTime: '', expectedReturnDateTime: '',
    parentName: '', parentPhone: '', parentRelation: '', urgent: false, additionalNotes: ''
  });

  useEffect(() => { fetchGatepasses(); }, []);

  const fetchGatepasses = async () => {
    try {
      const res = await api.get('/student/gatepasses');
      setGatepasses(res.data);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const set = (key, val) => setFormData(p => ({ ...p, [key]: val }));

  const handleApply = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await api.post('/student/gatepass', formData);
      setShowModal(false);
      fetchGatepasses();
      setFormData({ destination: '', reason: '', outDateTime: '', expectedReturnDateTime: '', parentName: '', parentPhone: '', parentRelation: '', urgent: false, additionalNotes: '' });
    } catch (err) {
      alert('Failed to apply: ' + (err.response?.data?.error || err.message));
    } finally { setSubmitting(false); }
  };

  return (
    <div className="app-layout">
      <Sidebar user={user} logout={logout} />
      <main className="page-content animate-fade-in">
        <div className="page-header">
          <div>
            <h1 className="page-title">My Gatepasses</h1>
            <p className="page-subtitle">Welcome back, {user?.name}</p>
          </div>
          <button className="btn btn-primary" onClick={() => setShowModal(true)}>
            <PlusCircle size={16} /> Apply for Gatepass
          </button>
        </div>

        <div className="card">
          <div className="card-header">
            <span className="card-title">Gatepass History</span>
            <span className="badge badge-primary">{gatepasses.length} total</span>
          </div>

          {loading ? (
            <div className="empty-state"><div className="empty-icon">⏳</div><div className="empty-text">Loading...</div></div>
          ) : gatepasses.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📋</div>
              <div className="empty-text">No gatepasses yet. Apply for one to get started!</div>
            </div>
          ) : (
            <div className="flex flex-col gap-3">
              {gatepasses.map(gp => (
                <div key={gp.id} className="gp-item">
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div className="gp-meta">
                      <span className="gp-number">{gp.gatepassNumber}</span>
                      <span className={getStatusClass(gp.status)}>{gp.status.replaceAll('_', ' ')}</span>
                      {gp.urgent && <span className="badge badge-danger">URGENT</span>}
                    </div>
                    <div className="gp-details">
                      <span className="gp-detail-item"><MapPin size={12} /> {gp.destination}</span>
                      <span className="gp-detail-item"><Clock size={12} /> {new Date(gp.outDateTime).toLocaleString()}</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    {gp.status === 'WARDEN_APPROVED' && gp.qrCodeImage && (
                      <img src={`data:image/png;base64,${gp.qrCodeImage}`} alt="QR" style={{ width: 64, height: 64, borderRadius: 8, border: '1px solid var(--border)' }} />
                    )}
                    {gp.status === 'PENDING' && (
                      <button className="btn btn-danger btn-sm" onClick={async () => {
                        if (window.confirm('Cancel this request?')) {
                          try {
                            await api.delete(`/student/gatepass/${gp.id}`);
                            fetchGatepasses();
                          } catch (err) {
                            alert('Failed to cancel: ' + (err.response?.data?.error || err.message));
                          }
                        }
                      }}>Cancel</button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>

      {showModal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setShowModal(false)}>
          <div className="modal">
            <div className="modal-header">
              <h3 style={{ fontSize: '1rem' }}>Apply for Gatepass</h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowModal(false)}><X size={14} /></button>
            </div>
            <form onSubmit={handleApply} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div className="form-group">
                <label className="label">Destination *</label>
                <input className="input-field" required value={formData.destination} onChange={e => set('destination', e.target.value)} placeholder="e.g. Home, Hospital..." />
              </div>
              <div className="form-group">
                <label className="label">Reason *</label>
                <textarea className="input-field" required value={formData.reason} onChange={e => set('reason', e.target.value)} rows={3} placeholder="Explain the purpose of your visit..." />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label className="label">Out Date / Time *</label>
                  <input type="datetime-local" className="input-field" required value={formData.outDateTime} onChange={e => set('outDateTime', e.target.value)} />
                </div>
                <div className="form-group">
                  <label className="label">Expected Return *</label>
                  <input type="datetime-local" className="input-field" required value={formData.expectedReturnDateTime} onChange={e => set('expectedReturnDateTime', e.target.value)} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label className="label">Parent / Guardian Name</label>
                  <input className="input-field" value={formData.parentName} onChange={e => set('parentName', e.target.value)} placeholder="Full name" />
                </div>
                <div className="form-group">
                  <label className="label">Parent Phone</label>
                  <input className="input-field" value={formData.parentPhone} onChange={e => set('parentPhone', e.target.value)} placeholder="+91 XXXXX XXXXX" />
                </div>
              </div>
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', cursor: 'pointer', padding: '0.75rem', background: 'var(--danger-bg)', borderRadius: 'var(--radius-md)', border: '1px solid rgba(239,68,68,0.2)' }}>
                <input type="checkbox" checked={formData.urgent} onChange={e => set('urgent', e.target.checked)} />
                <AlertTriangle size={15} style={{ color: 'var(--danger)' }} />
                <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--danger)' }}>Mark as Urgent Request</span>
              </label>
              <button type="submit" className="btn btn-primary" disabled={submitting} style={{ width: '100%', justifyContent: 'center', padding: '0.75rem' }}>
                {submitting ? 'Submitting...' : 'Submit Application'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default StudentDashboard;
