import api from '../api/axiosConfig';

export const jobCardService = {
  getAll: async () => {
    const response = await api.get('/job_cards');
    return response.data;
  },

  getById: async (id) => {
    const response = await api.get(`/job_cards/${id}`);
    return response.data;
  },

  getByAppointmentId: async (appointmentId) => {
    const response = await api.get(`/job_cards/appointment/${appointmentId}`);
    return response.data;
  },

  create: async (dto) => {
    const response = await api.post('/job_cards', dto);
    return response.data;
  },

  assignMechanic: async (id, mechanicId) => {
    const response = await api.put(`/job_cards/${id}/assign_mechanic`, { mechanicId });
    return response.data;
  },

  startWork: async (id) => {
    const response = await api.put(`/job_cards/${id}/start`);
    return response.data;
  },

  completeWork: async (id) => {
    const response = await api.put(`/job_cards/${id}/complete`);
    return response.data;
  },

  cancel: async (id, reason) => {
    const response = await api.delete(`/job_cards/${id}/cancel`, { data: { reason } });
    return response.data;
  },

  addItem: async (id, dto) => {
    const payload = {
      inventoryItemId: Number(dto.inventoryItemId || dto.inventoryId),
      quantity: Number(dto.quantity || dto.quantityUsed || 1)
    };
    const response = await api.post(`/job_cards/${id}/items`, payload);
    return response.data;
  },

  removeItem: async (jobCardId, itemId) => {
    const response = await api.delete(`/job_cards/${jobCardId}/items/${itemId}`);
    return response.data;
  },

  getByManager: async (managerId) => {
    const response = await api.get(`/job_cards/manager/${managerId}`);
    return response.data;
  },

  getByMechanic: async (mechanicId) => {
    const response = await api.get(`/job_cards/mechanic/${mechanicId}`);
    return response.data;
  },

  getManagerDashboard: async (managerId) => {
    const response = await api.get(`/job_cards/dashboard/manager/${managerId}`);
    return response.data;
  },

  getMechanicDashboard: async (mechanicId) => {
    const response = await api.get(`/job_cards/dashboard/mechanic/${mechanicId}`);
    return response.data;
  }
};
