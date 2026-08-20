import React, { useEffect, useState } from 'react';
import { Table, Button, Card, Badge, Spinner, Tab, Nav, Modal, Form } from 'react-bootstrap';
import { userService } from '../../services/userService';
import { toast } from 'react-toastify';

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeRoleFilter, setActiveRoleFilter] = useState('ALL');

  // Pagination State
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Modal State
  const [showAddModal, setShowAddModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    userName: '',
    email: '',
    password: '',
    userRole: 'CUSTOMER',
    mobile: '',
    salary: 0.0,
  });

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const data = await userService.getPaginated(currentPage, pageSize, activeRoleFilter);
      setUsers(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      toast.error('Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [currentPage, pageSize, activeRoleFilter]);

  const handleCreateUser = async (e) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      await userService.create({
        ...formData,
        salary: Number(formData.salary) || 1.1,
        active: true,
      });

      toast.success(`New ${formData.userRole} user created!`);
      setShowAddModal(false);
      setFormData({ userName: '', email: '', password: '', userRole: 'CUSTOMER', mobile: '', salary: 0.0 });
      fetchUsers();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create user');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    const targetId = Number(id);
    if (!targetId || isNaN(targetId)) {
      toast.error('Invalid user ID');
      return;
    }
    if (window.confirm('Are you sure you want to remove this user?')) {
      try {
        await userService.delete(targetId);
        toast.success('User removed.');
        fetchUsers();
      } catch (err) {
        console.error('User delete error:', err);
        toast.error(err.response?.data?.message || 'Failed to delete user.');
      }
    }
  };

  const filteredUsers = users;

  const getRoleBadge = (role) => {
    switch (role) {
      case 'ADMIN': return 'danger';
      case 'MANAGER': return 'warning';
      case 'MECHANIC': return 'info';
      case 'CUSTOMER': return 'success';
      default: return 'secondary';
    }
  };

  if (loading) {
    return (
      <div className="d-flex justify-content-center mt-5">
        <Spinner animation="border" variant="primary" />
      </div>
    );
  }

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h3 className="fw-bold mb-1">User Management Panel</h3>
          <p className="text-muted mb-0">Manage system roles, create new team members or customer accounts.</p>
        </div>
        <Button variant="primary" className="fw-semibold" onClick={() => setShowAddModal(true)}>
          <i className="bi bi-person-plus me-2"></i>Add New User
        </Button>
      </div>

      <Tab.Container activeKey={activeRoleFilter} onSelect={(k) => { setActiveRoleFilter(k); setCurrentPage(0); }}>
        <Card className="border-0 shadow-sm">
          <Card.Header className="bg-transparent border-0 pt-3 px-4">
            <Nav variant="tabs">
              <Nav.Item><Nav.Link eventKey="ALL" className="fw-semibold">All Users {activeRoleFilter === 'ALL' ? `(${totalElements})` : ''}</Nav.Link></Nav.Item>
              <Nav.Item><Nav.Link eventKey="ADMIN" className="fw-semibold">Admins {activeRoleFilter === 'ADMIN' ? `(${totalElements})` : ''}</Nav.Link></Nav.Item>
              <Nav.Item><Nav.Link eventKey="MANAGER" className="fw-semibold">Managers {activeRoleFilter === 'MANAGER' ? `(${totalElements})` : ''}</Nav.Link></Nav.Item>
              <Nav.Item><Nav.Link eventKey="MECHANIC" className="fw-semibold">Mechanics {activeRoleFilter === 'MECHANIC' ? `(${totalElements})` : ''}</Nav.Link></Nav.Item>
              <Nav.Item><Nav.Link eventKey="CUSTOMER" className="fw-semibold">Customers {activeRoleFilter === 'CUSTOMER' ? `(${totalElements})` : ''}</Nav.Link></Nav.Item>
            </Nav>
          </Card.Header>
          <Card.Body className="p-0">
            <Table responsive hover className="mb-0">
              <thead className="table-light">
                <tr>
                  <th className="ps-4">Username</th>
                  <th>Email Address</th>
                  <th>Mobile Number</th>
                  <th>Role</th>
                  <th className="text-end pe-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="text-center py-4 text-muted">No users found for this role.</td>
                  </tr>
                ) : (
                  filteredUsers.map((u) => {
                    const targetId = u.userId || u.id;
                    return (
                      <tr key={targetId}>
                        <td className="ps-4 align-middle fw-medium">{u.userName}</td>
                        <td className="align-middle">{u.email}</td>
                        <td className="align-middle">{u.mobile}</td>
                        <td className="align-middle">
                          <Badge bg={getRoleBadge(u.userRole || u.role)} className="px-2 py-1">
                            {u.userRole || u.role}
                          </Badge>
                        </td>
                        <td className="text-end pe-4 align-middle">
                          <Button variant="light" size="sm" className="text-danger border" onClick={() => handleDelete(targetId)}>
                            <i className="bi bi-trash"></i>
                          </Button>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </Table>
            {totalElements > 0 && (
              <div className="d-flex justify-content-between align-items-center p-3 border-top bg-light-subtle">
                <div className="text-muted small fw-medium">
                  Showing {currentPage * pageSize + 1} to {Math.min((currentPage + 1) * pageSize, totalElements)} of {totalElements} users
                </div>
                <div className="d-flex align-items-center gap-3">
                  <div className="d-flex align-items-center gap-2">
                    <span className="text-muted small">Show:</span>
                    <Form.Select 
                      size="sm" 
                      value={pageSize} 
                      onChange={(e) => { 
                        setPageSize(Number(e.target.value)); 
                        setCurrentPage(0); 
                      }}
                      style={{ width: '75px', cursor: 'pointer' }}
                    >
                      <option value={5}>5</option>
                      <option value={10}>10</option>
                      <option value={20}>20</option>
                      <option value={50}>50</option>
                    </Form.Select>
                  </div>
                  <div className="d-flex gap-1">
                    <Button 
                      variant="light" 
                      size="sm" 
                      className="border fw-semibold"
                      disabled={currentPage === 0} 
                      onClick={() => setCurrentPage(prev => prev - 1)}
                    >
                      <i className="bi bi-chevron-left me-1"></i>Previous
                    </Button>
                    <Button 
                      variant="light" 
                      size="sm" 
                      className="border fw-semibold"
                      disabled={currentPage >= totalPages - 1} 
                      onClick={() => setCurrentPage(prev => prev + 1)}
                    >
                      Next<i className="bi bi-chevron-right ms-1"></i>
                    </Button>
                  </div>
                </div>
              </div>
            )}
          </Card.Body>
        </Card>
      </Tab.Container>

      {/* Modal: Add User */}
      <Modal show={showAddModal} onHide={() => setShowAddModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title className="fw-bold">Create System User</Modal.Title>
        </Modal.Header>
        <Form onSubmit={handleCreateUser}>
          <Modal.Body>
            <Form.Group className="mb-3">
              <Form.Label className="fw-semibold">Username</Form.Label>
              <Form.Control 
                type="text" 
                required 
                value={formData.userName}
                onChange={(e) => setFormData({ ...formData, userName: e.target.value })}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label className="fw-semibold">Email Address</Form.Label>
              <Form.Control 
                type="email" 
                required 
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label className="fw-semibold">Password</Form.Label>
              <Form.Control 
                type="password" 
                required 
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label className="fw-semibold">Mobile Number</Form.Label>
              <Form.Control 
                type="text" 
                required 
                value={formData.mobile}
                onChange={(e) => setFormData({ ...formData, mobile: e.target.value })}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label className="fw-semibold">Assign Role</Form.Label>
              <Form.Select 
                value={formData.userRole}
                onChange={(e) => setFormData({ ...formData, userRole: e.target.value })}
              >
                <option value="CUSTOMER">CUSTOMER</option>
                <option value="MECHANIC">MECHANIC</option>
                <option value="MANAGER">MANAGER</option>
                <option value="ADMIN">ADMIN</option>
              </Form.Select>
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="light" className="border" onClick={() => setShowAddModal(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" disabled={submitting}>
              {submitting ? <Spinner size="sm" animation="border" /> : 'Create User'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </div>
  );
};

export default UserManagement;
