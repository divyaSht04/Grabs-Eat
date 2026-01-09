import { useEffect, useState } from 'react';
import { Users, Package, ShoppingBag, TrendingUp } from 'lucide-react';

interface DashboardStats {
  totalStaff: number;
  totalOrders: number;
  totalMenuItems: number;
  revenue: number;
}

const AdminDashboard = () => {
  const [stats, setStats] = useState<DashboardStats>({
    totalStaff: 0,
    totalOrders: 0,
    totalMenuItems: 0,
    revenue: 0,
  });

  useEffect(() => {
    // TODO: Fetch actual stats from API
    setStats({
      totalStaff: 12,
      totalOrders: 247,
      totalMenuItems: 48,
      revenue: 15420,
    });
  }, []);

  const statCards = [
    {
      title: 'Total Staff',
      value: stats.totalStaff,
      icon: Users,
      color: 'bg-blue-500',
      bgLight: 'bg-blue-50',
      textColor: 'text-blue-600',
    },
    {
      title: 'Total Orders',
      value: stats.totalOrders,
      icon: ShoppingBag,
      color: 'bg-green-500',
      bgLight: 'bg-green-50',
      textColor: 'text-green-600',
    },
    {
      title: 'Menu Items',
      value: stats.totalMenuItems,
      icon: Package,
      color: 'bg-purple-500',
      bgLight: 'bg-purple-50',
      textColor: 'text-purple-600',
    },
    {
      title: 'Revenue',
      value: `$${stats.revenue.toLocaleString()}`,
      icon: TrendingUp,
      color: 'bg-orange-500',
      bgLight: 'bg-orange-50',
      textColor: 'text-orange-600',
    },
  ];

  return (
    <div className="p-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-800">Dashboard</h1>
        <p className="text-gray-600 mt-2">Welcome to GrabEat Admin Panel</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {statCards.map(stat => {
          const Icon = stat.icon;
          return (
            <div
              key={stat.title}
              className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600 mb-1">{stat.title}</p>
                  <p className="text-3xl font-bold text-gray-800">{stat.value}</p>
                </div>
                <div className={`${stat.bgLight} p-3 rounded-lg`}>
                  <Icon className={stat.textColor} size={24} />
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Recent Activity Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Orders */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 className="text-xl font-bold text-gray-800 mb-4">Recent Orders</h2>
          <div className="space-y-3">
            {[1, 2, 3, 4, 5].map(order => (
              <div
                key={order}
                className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
              >
                <div>
                  <p className="font-medium text-gray-800">Order #{1000 + order}</p>
                  <p className="text-sm text-gray-500">Customer Name</p>
                </div>
                <div className="text-right">
                  <p className="font-medium text-gray-800">
                    ${(Math.random() * 50 + 10).toFixed(2)}
                  </p>
                  <span className="inline-block px-2 py-1 text-xs font-medium text-green-600 bg-green-50 rounded-full">
                    Completed
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Quick Actions */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 className="text-xl font-bold text-gray-800 mb-4">Quick Actions</h2>
          <div className="space-y-3">
            <button className="w-full text-left p-4 bg-orange-50 hover:bg-orange-100 rounded-lg transition-colors">
              <p className="font-medium text-orange-600">Add New Staff Member</p>
              <p className="text-sm text-gray-600 mt-1">Register a new staff account</p>
            </button>
            <button className="w-full text-left p-4 bg-blue-50 hover:bg-blue-100 rounded-lg transition-colors">
              <p className="font-medium text-blue-600">View All Orders</p>
              <p className="text-sm text-gray-600 mt-1">Manage and track orders</p>
            </button>
            <button className="w-full text-left p-4 bg-purple-50 hover:bg-purple-100 rounded-lg transition-colors">
              <p className="font-medium text-purple-600">Manage Menu</p>
              <p className="text-sm text-gray-600 mt-1">Add or edit menu items</p>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
