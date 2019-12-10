package com.xdlr.camera.token;

public class UserActionResponse {

	private  String userId;
	private  int userToken;
	private  int userAction;
	private  boolean successOnChain;

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setUserToken(int userToken) {
		this.userToken = userToken;
	}

	public void setUserAction(int userAction) {
		this.userAction = userAction;
	}

	public void setSuccessOnChain(boolean successOnChain) {
		this.successOnChain = successOnChain;
	}

	public static final int ACTION_TYPE_REGISTER = 0;
	public static final int ACTION_TYPE_GARBAGE = 1;
	public static final int ACTION_TYPE_VENDING_MACHINE = 2;

	public UserActionResponse() {
	}

	public UserActionResponse(String userId, int userToken, int userAction, boolean successOnChain) {
		this.userId = userId;
		this.userToken = userToken;
		this.userAction = userAction;
		this.successOnChain = successOnChain;
	}

	public String getUserId() {
		return userId;
	}

	public int getUserToken() {
		return userToken;
	}

	public int getUserAction() {
		return userAction;
	}

	public boolean isSuccessOnChain() {
		return successOnChain;
	}
}
