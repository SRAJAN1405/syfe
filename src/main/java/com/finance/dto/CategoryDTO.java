package com.finance.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoryDTO {

    public static class CreateRequest {
        @NotBlank(message = "Category name is required")
        private String name;

        @NotBlank(message = "Category type is required (INCOME or EXPENSE)")
        private String type;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class CategoryResponse {
        private Long id;
        private String name;
        private String type;
        private boolean isDefault;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public boolean isDefault() { return isDefault; }
        public void setDefault(boolean aDefault) { isDefault = aDefault; }
    }
}
