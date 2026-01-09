import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import { ProtectedAdminRoute, AdminLayout } from './components/admin';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import { RequestVerificationPage } from './pages/auth/RequestVerificationPage';
import { VerifyOTPPage } from './pages/auth/VerifyOTPPage';
import { ForgotPasswordRequestPage } from './pages/auth/ForgotPasswordRequestPage';
import { ResetPasswordWithOTPPage } from './pages/auth/ResetPasswordWithOTPPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import { AdminDashboard, StaffManagement, Settings } from './pages/admin';

function App() {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Email Verification Flow */}
          <Route path="/request-verification" element={<RequestVerificationPage />} />
          <Route path="/verify-email" element={<VerifyOTPPage />} />

          {/* Password Reset Flow */}
          <Route path="/forgot-password" element={<ForgotPasswordRequestPage />} />
          <Route path="/reset-password" element={<ResetPasswordWithOTPPage />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<DashboardPage />} />
          </Route>

          {/* Admin Routes */}
          <Route
            path="/admin"
            element={
              <ProtectedAdminRoute>
                <AdminLayout />
              </ProtectedAdminRoute>
            }
          >
            <Route index element={<Navigate to="/admin/dashboard" replace />} />
            <Route path="dashboard" element={<AdminDashboard />} />
            <Route path="staff" element={<StaffManagement />} />
            <Route path="settings" element={<Settings />} />
          </Route>

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </Router>
  );
}

export default App;
