import { Navigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { Role } from '../../types/auth.types';

const ProtectedAdminRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated, isLoading, user } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-500"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Only allow OWNER and STAFF to access admin routes
  if (user?.role !== Role.OWNER && user?.role !== Role.STAFF) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

export default ProtectedAdminRoute;
