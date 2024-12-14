package com.example.dto;

public enum TokenType {
	BEARER("Bearer ");

	private String type;

	private TokenType(String type) {
		this.type = type;
	}

	public String value() {
		return this.type;
	};
}
