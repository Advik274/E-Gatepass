import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';

// Pages
import Login from './pages/Login';
import StudentDashboard from './pages/student/Dashboard';
import CoordinatorDashboard from './pages/coordinator/Dashboard';
import WardenDashboard from './pages/warden/Dashboard';
import SecurityScanner from './pages/security/Scanner';
import AdminDashboard from './pages/admin/Dashboard';

// Protected Route Component
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, loading } = useAuth();

  if (loading) return <div className="flex justify-center items-center" style={{height: '100vh'}}>Loading...</div>;

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

// Route redirect based on role
const RoleBasedRedirect = () => {
  const { user, loading } = useAuth();
  
  if (loading) return <div>Loading...</div>;
  if (!user) return <Navigate to="/login" replace />;
  
  switch(user.role) {
    case 'STUDENT': return <Navigate to="/student" replace />;
    case 'COORDINATOR': return <Navigate to="/coordinator" replace />;
    case 'WARDEN': return <Navigate to="/warden" replace />;
    case 'SECURITY': return <Navigate to="/security" replace />;
    case 'ADMIN': return <Navigate to="/admin" replace />;
    default: return <Navigate to="/login" replace />;
  }
};

const App = () => {
  return (
    <AuthProvider>
      <Router>
        <div className="app-container">
          {/* Navigation/Header could go here */}
          <main className="main-content">
            <Routes>
              <Route path="/login" element={<Login />} />
              
              <Route path="/" element={<RoleBasedRedirect />} />

              <Route path="/student/*" element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentDashboard />
                </ProtectedRoute>
              } />

              <Route path="/coordinator/*" element={
                <ProtectedRoute allowedRoles={['COORDINATOR']}>
                  <CoordinatorDashboard />
                </ProtectedRoute>
              } />

              <Route path="/warden/*" element={
                <ProtectedRoute allowedRoles={['WARDEN']}>
                  <WardenDashboard />
                </ProtectedRoute>
              } />

              <Route path="/security/*" element={
                <ProtectedRoute allowedRoles={['SECURITY']}>
                  <SecurityScanner />
                </ProtectedRoute>
              } />

              <Route path="/admin/*" element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminDashboard />
                </ProtectedRoute>
              } />

              <Route path="/unauthorized" element={<div className="container"><h2>Unauthorized Access</h2></div>} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>
        </div>
      </Router>
    </AuthProvider>
  );
};

export default App;
