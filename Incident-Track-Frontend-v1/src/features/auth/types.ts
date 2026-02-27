export type UserRole = "EMPLOYEE" | "MANAGER" | "ADMIN";

export type UserStatus = "ACTIVE" | "INACTIVE";

export interface AuthRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  email: string;
  userId: number;
  role: UserRole;
}

export interface UserResponseDto {
  username: string;
  email: string;
  userId: number;
  role: UserRole;
  departmentId: number;
  status: UserStatus;
}

export interface UserRegistrationDTO {
  username: string;
  email: string;
  password: string;
  departmentId: number;
  role: UserRole;
  status: UserStatus;
}

export interface UpdateUserDetails {
  username: string;
  department?: string; // you have both department + departmentId in backend
  role: UserRole;
  password?: string;
  email: string;
  departmentId: number;
}

/** Matches backend UpdateUser DTO: /api/auth/updateUser (self-service) */
export interface UpdateUserSelf {
  username: string;
  password?: string;
}