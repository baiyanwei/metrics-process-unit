package com.secpro.platform.monitoring.process.entity;
/**
 * 基线bean
 * @author sxf
 *
 */
public class BaselineBean {
	private long id;
	private String baselineType;
	private String baselineBlackWhite;
	private String rule;
	private int score;
	private String baselineDesc;
	public String getBaselineDesc() {
		return baselineDesc;
	}
	public void setBaselineDesc(String baselineDesc) {
		this.baselineDesc = baselineDesc;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getBaselineType() {
		return baselineType;
	}
	public void setBaselineType(String baselineType) {
		this.baselineType = baselineType;
	}
	public String getBaselineBlackWhite() {
		return baselineBlackWhite;
	}
	public void setBaselineBlackWhite(String baselineBlackWhite) {
		this.baselineBlackWhite = baselineBlackWhite;
	}
	
	public String getRule() {
		return rule;
	}
	public void setRule(String rule) {
		this.rule = rule;
	}
	
}
