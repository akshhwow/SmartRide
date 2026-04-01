import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminAPI } from '../services/api';
import { toast } from 'react-toastify';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer,
  BarChart, Bar
} from 'recharts';
import './AdminDashboard.css';

const AdminDashboard = () => {
  const [reports, setReports] = useState(null);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Search & Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);

  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('token');
    const userRole = JSON.parse(localStorage.getItem('user'))?.role;

    if (!token || userRole !== 'ADMIN') {
      toast.error('Unauthorized access. Admin only.');
      navigate('/login');
      return;
    }

    fetchDashboardData(0);
  }, [navigate]);

  const fetchDashboardData = async (page) => {
    setLoading(true);
    setError(null);
    try {
      const [reportsRes, usersRes] = await Promise.all([
        adminAPI.getReports(),
        adminAPI.getUsers(page, 10),
      ]);

      setReports(reportsRes.data.data);
      setUsers(usersRes.data.data.content || []);
      setTotalPages(usersRes.data.data.totalPages || 0);
      setCurrentPage(page);
    } catch (err) {
      console.error(err);
      setError("Failed to load dashboard data.");
      if (err.response?.status === 401 || err.response?.status === 403) {
        navigate('/login');
      }
    } finally {
      setLoading(false);
    }
  };

  const confirmToggleStatus = (user) => {
    setSelectedUser(user);
    setIsModalOpen(true);
  };

  const executeToggleStatus = async () => {
    if (!selectedUser) return;
    
    try {
      await adminAPI.toggleUserStatus(selectedUser.id, !selectedUser.isActive);
      toast.success(`User ${selectedUser.isActive ? 'blocked' : 'unblocked'} successfully`);
      fetchDashboardData(currentPage);
    } catch (err) {
      toast.error('Failed to update user status');
    } finally {
      setIsModalOpen(false);
      setSelectedUser(null);
    }
  };

  const filteredUsers = useMemo(() => {
    return users.filter(user => {
      const matchesSearch = 
        (user.fullName?.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (user.email?.toLowerCase().includes(searchTerm.toLowerCase()));
      
      const matchesRole = roleFilter === 'ALL' || user.role === roleFilter;

      return matchesSearch && matchesRole;
    });
  }, [users, searchTerm, roleFilter]);

  // Mock data for Line Chart
  const revenueData = [
    { name: 'Mon', revenue: 4000 },
    { name: 'Tue', revenue: 3000 },
    { name: 'Wed', revenue: 5000 },
    { name: 'Thu', revenue: 2780 },
    { name: 'Fri', revenue: 8900 },
    { name: 'Sat', revenue: 12000 },
    { name: 'Sun', revenue: 14000 },
  ];

  if (loading && !reports) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gray-50">
        <p className="text-gray-500 font-medium">Loading dashboard...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 font-sans text-gray-900">
      
      {/* Header */}
      <header className="bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center sticky top-0 z-10">
        <h1 className="text-2xl font-semibold text-gray-800">Admin Panel</h1>
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium text-gray-700">System Admin</span>
          <div className="w-8 h-8 bg-gray-200 text-gray-600 rounded-full flex items-center justify-center font-bold text-sm">
            A
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto p-6 space-y-6">
        
        {/* Error State */}
        {error && (
          <div className="bg-red-50 text-red-600 p-3 rounded-md text-sm font-medium border border-red-200 flex justify-between">
            <span>{error}</span>
            <button onClick={() => fetchDashboardData(currentPage)} className="underline">Retry</button>
          </div>
        )}

        {/* 4 Stat Cards */}
        {reports && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-5 hover:shadow-md transition">
              <div className="flex items-center gap-2 mb-2 text-gray-500">
                <svg width="16" height="16" className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                <span className="text-sm font-medium">Total Earnings</span>
              </div>
              <p className="text-2xl font-bold text-gray-900">₹{Math.round(reports.totalPlatformEarnings || 0).toLocaleString()}</p>
            </div>

            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-5 hover:shadow-md transition">
              <div className="flex items-center gap-2 mb-2 text-gray-500">
                <svg width="16" height="16" className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path></svg>
                <span className="text-sm font-medium">Revenue Today</span>
              </div>
              <p className="text-2xl font-bold text-gray-900">₹{Math.round(reports.revenueToday || 0).toLocaleString()}</p>
            </div>

            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-5 hover:shadow-md transition">
              <div className="flex items-center gap-2 mb-2 text-gray-500">
                <svg width="16" height="16" className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
                <span className="text-sm font-medium">Active Users</span>
              </div>
              <p className="text-2xl font-bold text-gray-900">{reports.activeUsers || 0}</p>
            </div>

            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-5 hover:shadow-md transition">
              <div className="flex items-center gap-2 mb-2 text-gray-500">
                <svg width="16" height="16" className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
                <span className="text-sm font-medium">Total Rides</span>
              </div>
              <p className="text-2xl font-bold text-gray-900">{reports.totalRides || 0}</p>
            </div>
          </div>
        )}

        {/* Charts Section */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="bg-white p-5 rounded-lg shadow-sm border border-gray-200">
            <h3 className="text-sm font-semibold text-gray-700 mb-4">Revenue Overview</h3>
            <div className="h-[250px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={revenueData} margin={{ top: 5, right: 20, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                  <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#6B7280', fontSize: 12}} dy={10} />
                  <YAxis axisLine={false} tickLine={false} tick={{fill: '#6B7280', fontSize: 12}} tickFormatter={(value) => `₹${value}`} dx={-10} />
                  <RechartsTooltip 
                    contentStyle={{ borderRadius: '6px', border: '1px solid #E5E7EB', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}
                    formatter={(value) => [`₹${value}`, 'Revenue']}
                  />
                  <Line type="monotone" dataKey="revenue" stroke="#10B981" strokeWidth={2} dot={{r: 3}} activeDot={{r: 5}} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="bg-white p-5 rounded-lg shadow-sm border border-gray-200">
            <h3 className="text-sm font-semibold text-gray-700 mb-4">Top Drivers by Rating</h3>
            <div className="h-[250px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={reports?.topDrivers || []} layout="vertical" margin={{ top: 5, right: 10, left: 10, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#E5E7EB" />
                  <XAxis type="number" domain={[0, 5]} hide />
                  <YAxis dataKey="id" type="category" axisLine={false} tickLine={false} tickFormatter={(val) => `Driver #${val}`} width={70} tick={{fontSize: 12}} />
                  <RechartsTooltip cursor={{fill: '#F9FAFB'}} formatter={(value) => [`${value} ⭐`, 'Rating']} />
                  <Bar dataKey="rating" fill="#3B82F6" radius={[0, 4, 4, 0]} barSize={20} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        {/* Users Table Section */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          
          <div className="p-4 border-b border-gray-200 flex items-center justify-between gap-4">
            <h2 className="text-base font-semibold text-gray-800">Users</h2>
            
            <div className="flex gap-3 w-2/3 md:w-1/2">
              <input
                type="text"
                placeholder="Search users..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full p-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
              <select
                value={roleFilter}
                onChange={(e) => setRoleFilter(e.target.value)}
                className="w-32 p-2 border border-gray-300 rounded-md text-sm bg-white focus:outline-none focus:ring-1 focus:ring-blue-500"
              >
                <option value="ALL">All Roles</option>
                <option value="DRIVER">Drivers</option>
                <option value="PASSENGER">Passengers</option>
                <option value="ADMIN">Admins</option>
              </select>
            </div>
          </div>
          
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm whitespace-nowrap">
              <thead className="bg-gray-100 text-gray-600 font-medium border-b border-gray-200">
                <tr>
                  <th className="px-4 py-3">User</th>
                  <th className="px-4 py-3">Email</th>
                  <th className="px-4 py-3">Role</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filteredUsers.length > 0 ? (
                  filteredUsers.map(user => (
                    <tr key={user.id} className="hover:bg-gray-50 transition">
                      <td className="px-4 py-3">
                        <div className="font-medium text-gray-800">{user.fullName || 'N/A'}</div>
                        <div className="text-xs text-gray-400">ID: {user.id}</div>
                      </td>
                      <td className="px-4 py-3 text-gray-600">{user.email}</td>
                      <td className="px-4 py-3">
                        <span className={`px-2 py-0.5 rounded text-xs font-medium border ${
                          user.role === 'ADMIN' ? 'bg-purple-50 text-purple-700 border-purple-200' :
                          user.role === 'DRIVER' ? 'bg-blue-50 text-blue-700 border-blue-200' :
                          'bg-gray-50 text-gray-700 border-gray-200'
                        }`}>
                          {user.role}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <span className={`flex items-center gap-1.5 text-xs font-medium ${user.isActive ? 'text-green-600' : 'text-red-500'}`}>
                          <span className={`w-1.5 h-1.5 rounded-full ${user.isActive ? 'bg-green-500' : 'bg-red-500'}`}></span>
                          {user.isActive ? 'Active' : 'Blocked'}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right">
                        {user.role !== 'ADMIN' && (
                          <button
                            onClick={() => confirmToggleStatus(user)}
                            className={`px-3 py-1 rounded-md text-sm font-medium text-white transition ${
                              user.isActive 
                                ? 'bg-red-500 hover:bg-red-600' 
                                : 'bg-green-500 hover:bg-green-600'
                            }`}
                          >
                            {user.isActive ? 'Block' : 'Unblock'}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="5" className="px-4 py-8 text-center text-gray-500 text-sm">
                      No users found matching your filters.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          <div className="px-4 py-3 border-t border-gray-200 flex items-center justify-between bg-white text-sm">
            <span className="text-gray-500">
              Page {currentPage + 1} of {totalPages || 1}
            </span>
            <div className="flex gap-2">
              <button 
                disabled={currentPage === 0}
                onClick={() => fetchDashboardData(currentPage - 1)}
                className="px-3 py-1.5 border border-gray-300 rounded-md text-gray-600 disabled:opacity-50 hover:bg-gray-50 transition"
              >
                Previous
              </button>
              <button 
                disabled={currentPage >= totalPages - 1}
                onClick={() => fetchDashboardData(currentPage + 1)}
                className="px-3 py-1.5 border border-gray-300 rounded-md text-gray-600 disabled:opacity-50 hover:bg-gray-50 transition"
              >
                Next
              </button>
            </div>
          </div>
        </div>

      </div>

      {/* Confirmation Modal */}
      {isModalOpen && selectedUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
          <div className="bg-white rounded-lg shadow-xl max-w-sm w-full p-5">
            <h3 className="text-lg font-semibold text-gray-800 mb-2">
              Are you sure?
            </h3>
            <p className="text-gray-600 text-sm mb-6">
              You are about to {selectedUser.isActive ? 'block' : 'unblock'} <strong>{selectedUser.email}</strong>. This action can be reversed at any time.
            </p>
            <div className="flex justify-end gap-3">
              <button 
                onClick={() => setIsModalOpen(false)}
                className="px-4 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-md transition"
              >
                Cancel
              </button>
              <button 
                onClick={executeToggleStatus}
                className={`px-4 py-2 text-sm text-white rounded-md transition ${
                  selectedUser.isActive 
                    ? 'bg-red-500 hover:bg-red-600' 
                    : 'bg-green-500 hover:bg-green-600'
                }`}
              >
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}
      
    </div>
  );
};

export default AdminDashboard;
