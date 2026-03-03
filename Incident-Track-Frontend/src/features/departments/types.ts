export interface DepartmentRequestDto {
  departmentName: string;
}

// backend might return depatmentId (typo) OR departmentId
export interface DepartmentResponseDto {
  depatmentId?: number;
  departmentId?: number;
  departmentName: string;
}

export const getDepartmentId = (d: DepartmentResponseDto) =>
  d.departmentId ?? d.depatmentId ?? 0;