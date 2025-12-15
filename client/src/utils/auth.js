export const logout = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("user"); // if exists
};

export const isAuthenticated = () => {
  return !!localStorage.getItem("token");
};



