package com.daimler.data.db.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeServerLeanGovernceFeilds implements Serializable {
	private static final long serialVersionUID = 1L;
	private String description;
	private String classificationType;
	private String division;
	private String subDivision;
	private String department;
	private String archerId;
	private String procedureID;
	private String permission;
	private List<String> tags;
	private String typeOfProject;
	private Boolean piiData;
}
