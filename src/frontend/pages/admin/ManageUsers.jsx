import { useEffect, useState } from 'react';
import { adminApi } from '../../api/adminApi';
import Button from '../../components/Button';
import { ConfirmModal } from '../../components/Modal';
import { Loader } from '../../components/Loader';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { asArray, getErrorMessage } from '../../utils/errors';
import { DEFAULT_USER_IMAGE, assetUrl, useFallbackImage } from '../../utils/images';

function roleLabel(role) {
  return String(role || 'USER').replace('ROLE_', '');
}

export default function ManageUsers() {
  const { user, role } = useAuth();
  const { showToast } = useToast();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [savingId, setSavingId] = useState(null);
  const [deleteAccount, setDeleteAccount] = useState(null);

  const loadUsers = () => {
    setLoading(true);
    adminApi
      .getUsers()
      .then(({ data }) => setUsers(asArray(data)))
      .catch((error) => showToast(getErrorMessage(error), 'error'))
      .finally(() => setLoading(false));
  };

  useEffect(loadUsers, []);

  const updateRole = async (account, nextRole) => {
    setSavingId(account.id);
    try {
      const { data } = await adminApi.updateUserRole(account.id, nextRole);
      setUsers((current) => current.map((item) => (item.id === account.id ? data : item)));
      showToast('User role updated', 'success');
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setSavingId(null);
    }
  };

  const deleteUser = async () => {
    if (!deleteAccount) return;
    setSavingId(deleteAccount.id);
    try {
      await adminApi.deleteUser(deleteAccount.id);
      setUsers((current) => current.filter((item) => item.id !== deleteAccount.id));
      showToast('User deleted successfully', 'success');
      setDeleteAccount(null);
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setSavingId(null);
    }
  };

  if (loading) return <Loader label="Loading users" />;

  return (
    <section className="admin-page">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Accounts</span>
          <h1>Manage Users</h1>
        </div>
      </div>
      <div className="glass admin-panel">
        <h2>Current administrator</h2>
        <div className="detail-list">
          <span>Name: {user?.firstName || 'Admin'} {user?.lastName || ''}</span>
          <span>Email: {user?.email || 'Unavailable'}</span>
          <span>Role: {role || 'ADMIN'}</span>
        </div>
      </div>
      <div className="table-wrap glass">
        <table>
          <thead>
            <tr>
              <th>User</th>
              <th>ID</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th>Joined</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {users.map((account) => {
              const currentRole = roleLabel(account.role);
              const isCurrentUser = account.email === user?.email;
              return (
                <tr key={account.id}>
                  <td>
                    <div className="user-cell">
                      <img src={assetUrl(account.imageUrl, DEFAULT_USER_IMAGE)} alt={account.firstName || account.email || 'User'} onError={(event) => useFallbackImage(event, DEFAULT_USER_IMAGE)} />
                      <strong>{`${account.firstName || ''} ${account.lastName || ''}`.trim() || 'Unnamed user'}</strong>
                    </div>
                  </td>
                  <td>#{account.id}</td>
                  <td>{account.email}</td>
                  <td><span className="status-pill">{currentRole}</span></td>
                  <td><span className={`status-pill ${account.enabled ? 'success' : 'muted'}`}>{account.enabled ? 'Verified' : 'Pending'}</span></td>
                  <td>{account.createdAt ? new Date(account.createdAt).toLocaleDateString('en-IN') : 'N/A'}</td>
                  <td>
                    {!isCurrentUser ? (
                      <div className="table-actions">
                        {currentRole !== 'ADMIN' && <Button className="user-action-btn" variant="ghost" disabled={savingId === account.id} onClick={() => updateRole(account, 'ADMIN')}>Make admin</Button>}
                        {currentRole !== 'USER' && <Button className="user-action-btn" variant="ghost" disabled={savingId === account.id} onClick={() => updateRole(account, 'USER')}>Make user</Button>}
                        {currentRole !== 'SUPERADMIN' && <Button className="user-action-btn" variant="ghost" disabled={savingId === account.id} onClick={() => updateRole(account, 'SUPERADMIN')}>Make superadmin</Button>}
                        <Button className="user-action-btn" variant="danger" disabled={savingId === account.id} onClick={() => setDeleteAccount(account)}>Delete</Button>
                      </div>
                    ) : (
                      <span className="status-pill muted">Current account</span>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
        {!users.length && <div className="empty-state">No users found.</div>}
      </div>
      <ConfirmModal
        open={Boolean(deleteAccount)}
        title="Delete user?"
        message={`This will permanently delete ${deleteAccount?.email || 'this user'} and related account data.`}
        onClose={() => setDeleteAccount(null)}
        onConfirm={deleteUser}
        loading={savingId === deleteAccount?.id}
      />
    </section>
  );
}
