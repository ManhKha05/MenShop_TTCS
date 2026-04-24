const API_DOMAIN = "http://localhost:8080/";

const refreshAccessToken = async () => {
  console.log("Refresh Token");
  try {
    const res = await fetch(API_DOMAIN + "auth/refresh", {
      method: "POST",
      credentials: "include",
    });

    if (!res.ok) {
      throw new Error("Refresh failed");
    }

    const data = await res.json();
    localStorage.setItem("token", data.accessToken);
    return data.accessToken;
  } catch (error) {
    console.log(error);
    localStorage.removeItem("token");
    return null;
  }
};

const fetchBase = async (path, options = {}) => {
  console.log("fetch api: ", path);

  const token = localStorage.getItem("token");

  const headers = {
    ...(options.headers || {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };

  let res = await fetch(API_DOMAIN + path, {
    ...options,
    headers,
    credentials: "include",
  });

  if (res.status === 401 && !path.startsWith("auth/")) {
    const newToken = await refreshAccessToken();

    if (!newToken) {
      localStorage.removeItem("token");
      return res;
    }

    res = await fetch(API_DOMAIN + path, {
      ...options,
      headers: {
        ...(options.headers || {}),
        Authorization: `Bearer ${newToken}`,
      },
      credentials: "include",
    });

    if (res.status === 401) {
      localStorage.removeItem("token");
    }
  }

  return res;
};

export const get = async (path, params = {}) => {
  const query = new URLSearchParams(
    Object.entries(params).filter(
      ([_, value]) => value !== null && value !== undefined && value !== ""
    )
  ).toString();

  const url = query ? `${path}?${query}` : path;

  return fetchBase(url, { method: "GET" });
}

export const post = async (path, options) => {
  return fetchBase(path, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(options)
  }); 
}

export const put = async (path, options) => {
  return fetchBase(path, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(options)
  });
}

export const patch = async (path, options) => {
  return fetchBase(path, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(options)
  });
}

export const del = async (path) => {
  return fetchBase(path, { method: "DELETE" });;
}

