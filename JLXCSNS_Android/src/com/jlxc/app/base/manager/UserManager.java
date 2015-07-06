package com.jlxc.app.base.manager;

import com.jlxc.app.base.model.UserModel;

/**
 * 用户Manager
 */
public class UserManager {

	private UserModel user;
	
	private static UserManager userManager;

	public synchronized static UserManager getInstance() {
		if (userManager == null) {
			userManager = new UserManager();
			userManager.user = new UserModel();
		}
		return userManager;
	}

	private UserManager() {
	}

	public UserModel getUser() {
		return user;
	}

	public void setUser(UserModel user) {
		this.user = user;
	}
	

//	public void setCurrLoginUser(Context context, User currLoginUser) {
//		DbUtils db = DBManager.getInstance().getDB(context);
//		User dbUser;
//		try {
//			if (null != currLoginUser) {
//				dbUser = db.findFirst(User.class);
//				if (null == dbUser) {
//					db.save(currLoginUser);
//				} else {
//					db.update(currLoginUser);
//				}
//			}
//		} catch (DbException e) {
//			e.printStackTrace();
//		}
//		this.currLoginUser = currLoginUser;
//	}
}
