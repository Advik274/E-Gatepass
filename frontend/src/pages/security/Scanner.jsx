import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import { Html5QrcodeScanner } from 'html5-qrcode';
import { Camera, LogOut, Search, UserCheck, CheckCircle2, XCircle, Shield } from 'lucide-react';

const Sidebar = ({ user, logout, tab, setTab }) => (
  <aside className="sidebar">
    <div className="sidebar-brand">
      <div className="sidebar-brand-icon">🛡️</div>
      <div>
        <div className="sidebar-brand-text">E-Gatepass</div>
        <div className="sidebar-brand-sub">Security</div>
      </div>
    </div>
    <nav className="sidebar-nav">
      <span className="sidebar-section-label">Terminal</span>
      <button className={`sidebar-item ${tab === 'scanner' ? 'active' : ''}`} onClick={() => setTab('scanner')}><Camera size={16} /> QR Scanner</button>
      <button className={`sidebar-item ${tab === 'outside' ? 'active' : ''}`} onClick={() => setTab('outside')}><UserCheck size={16} /> Currently Outside</button>
    </nav>
    <div className="sidebar-footer">
      <div className="user-chip" style={{ marginBottom: '0.5rem' }}>
        <div className="user-avatar">{user?.name?.[0]?.toUpperCase()}</div>
        <div className="user-info">
          <div className="user-name">{user?.name}</div>
          <div className="user-role">Security</div>
        </div>
      </div>
      <button className="sidebar-item" onClick={logout} style={{ width: '100%' }}><LogOut size={16} /> Sign Out</button>
    </div>
  </aside>
);

const SecurityScanner = () => {
  const { user, logout } = useAuth();
  const [scanResult, setScanResult] = useState(null);
  const [manualInput, setManualInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [activeGatepasses, setActiveGatepasses] = useState([]);
  const [tab, setTab] = useState('scanner');

  useEffect(() => {
    if (tab === 'scanner') {
      const scanner = new Html5QrcodeScanner('qr-reader', { qrbox: { width: 240, height: 240 }, fps: 5 });
      scanner.render(onScanSuccess, () => {});
      return () => { scanner.clear().catch(() => {}); };
    } else {
      fetchOutside();
    }
  }, [tab]);

  const fetchOutside = async () => {
    try { const res = await api.get('/security/outside'); setActiveGatepasses(res.data); }
    catch (err) { console.error(err); }
  };

  const onScanSuccess = (text) => handleVerify({ qrCode: text });

  const handleManualSubmit = (e) => {
    e.preventDefault();
    if (!manualInput) return;
    handleVerify({ gatepassNumber: manualInput });
  };

  const handleVerify = async (payload) => {
    setLoading(true); setScanResult(null);
    try {
      const res = await api.post('/security/scan', payload);
      setScanResult(res.data);
      if (res.data.valid) setManualInput('');
    } catch (err) {
      setScanResult({ valid: false, message: 'Server error or network issue' });
    } finally { setLoading(false); }
  };

  const getResultClass = (r) => {
    if (!r.valid) return 'scan-result invalid';
    return r.action === 'EXIT' ? 'scan-result valid-exit' : 'scan-result valid-entry';
  };

  return (
    <div className="app-layout">
      <Sidebar user={user} logout={logout} tab={tab} setTab={setTab} />
      <main className="page-content animate-fade-in">
        <div className="page-header">
          <div>
            <h1 className="page-title">Security Terminal</h1>
            <p className="page-subtitle">{user?.designation || 'Gate Security'}</p>
          </div>
          <div className="flex items-center gap-2">
            <Shield size={16} style={{ color: 'var(--success)' }} />
            <span style={{ fontSize: '0.8rem', color: 'var(--success)', fontWeight: 600 }}>Active</span>
          </div>
        </div>

        {tab === 'scanner' && (
          <div className="flex flex-col gap-4">
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div className="card">
                <div className="card-header"><span className="card-title">Scan QR Code</span></div>
                <div id="qr-reader" style={{ width: '100%', borderRadius: 'var(--radius-md)', overflow: 'hidden' }} />
              </div>

              <div className="flex flex-col gap-4">
                <div className="card">
                  <div className="card-header"><span className="card-title">Manual Entry</span></div>
                  <form onSubmit={handleManualSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    <div className="form-group">
                      <label className="label">Gatepass Number</label>
                      <input type="text" className="input-field" placeholder="e.g. GP-20260515-0001" value={manualInput} onChange={e => setManualInput(e.target.value)} />
                    </div>
                    <button type="submit" className="btn btn-primary" disabled={loading} style={{ width: '100%', justifyContent: 'center' }}>
                      <Search size={15} /> {loading ? 'Verifying...' : 'Verify Gatepass'}
                    </button>
                  </form>
                </div>

                {scanResult && (
                  <div className={getResultClass(scanResult)}>
                    <div className="flex items-center gap-2 scan-result-title">
                      {scanResult.valid ? <CheckCircle2 size={20} /> : <XCircle size={20} />}
                      {scanResult.message}
                    </div>
                    {scanResult.valid && scanResult.gatepass && (
                      <div>
                        <div className="scan-detail-row"><span>Student</span><strong>{scanResult.gatepass.studentName}</strong></div>
                        <div className="scan-detail-row"><span>Roll No.</span><strong>{scanResult.gatepass.studentRollNumber}</strong></div>
                        <div className="scan-detail-row"><span>Destination</span><strong>{scanResult.gatepass.destination}</strong></div>
                        <div className="scan-detail-row"><span>Expected Return</span><strong>{new Date(scanResult.gatepass.expectedReturnDateTime).toLocaleString()}</strong></div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {tab === 'outside' && (
          <div className="card animate-fade-in">
            <div className="card-header">
              <span className="card-title">Students Currently Outside</span>
              <span className="badge badge-cyan">{activeGatepasses.length}</span>
            </div>
            {activeGatepasses.length === 0 ? (
              <div className="empty-state">
                <div className="empty-icon">🏠</div>
                <div className="empty-text">No students are currently outside.</div>
              </div>
            ) : (
              <div className="flex flex-col gap-3">
                {activeGatepasses.map(gp => (
                  <div key={gp.id} className="student-outside-card">
                    <div>
                      <div style={{ fontWeight: 600, color: 'var(--text-primary)', fontSize: '0.875rem' }}>{gp.studentName}</div>
                      <div style={{ fontSize: '0.775rem', color: 'var(--text-muted)', marginTop: '2px' }}>{gp.studentRollNumber} · {gp.destination}</div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontFamily: 'var(--font-mono)', fontSize: '0.75rem', color: 'var(--accent)' }}>{gp.gatepassNumber}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                        Return: {new Date(gp.expectedReturnDateTime).toLocaleString()}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
};

export default SecurityScanner;
