export interface CategoryRequestDto {
  categoryName: string;
  subCategory?: string;
  slaTimeHours?: number;
  isVisible?: boolean;
  departmentId: number;
}

export interface CategoryResponseDto {
  categoryId: number;
  categoryName: string;
  subCategory?: string;
  slaTimeHours?: number;
  isVisible?: boolean;
  departmentName?: string;
}