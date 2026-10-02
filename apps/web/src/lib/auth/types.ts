export type MeResponse = {
  id: string;
  tenant: {
    id: string;
    name: string;
    slug: string;
  };
  name: string;
  email: string;
  roles: string[];
  permissions: string[];
};

export type TokenResponse = {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
};
