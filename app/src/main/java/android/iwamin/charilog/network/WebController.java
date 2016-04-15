package android.iwamin.charilog.network;

import android.content.Context;
import android.iwamin.charilog.entity.CyclingRecord;
import android.iwamin.charilog.network.param.GetRequestCyclingRecord;
import android.iwamin.charilog.network.param.PostRequestCyclingRecord;
import android.iwamin.charilog.network.param.PostRequestUserCreate;
import android.iwamin.charilog.network.task.CyclingRecordGetTask;
import android.iwamin.charilog.network.task.CyclingRecordPostTask;
import android.iwamin.charilog.network.task.UserCreatePostTask;
import android.iwamin.charilog.repository.RepositoryReader;
import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;
import java.security.MessageDigest;
import java.util.List;

public class WebController {
	private static WebController _instance;

	private WebController() {

	}

	public static WebController getInstance() {
		if (_instance == null) {
			_instance = new WebController();
		}
		return _instance;
	}

	public void synchronize(Context context, String serverUrl) {
		try {
			URL url = new URL("http://" + serverUrl + "/record");
//			URL testUrl = new URL("http://" + serverUrl + "/test");

			// POST
			List<CyclingRecord> list = new RepositoryReader().getCyclingRecordList(context);
			for (CyclingRecord record : list) {
				PostRequestCyclingRecord param = new PostRequestCyclingRecord();
				param.setUserId("nobi");
				param.setDeviceId("nobi_device");
				param.setUrl(url);
				param.setRecord(record);
				AsyncTask<PostRequestCyclingRecord, Void, String> postTask
						= new CyclingRecordPostTask().execute(param);
				String response = postTask.get();
			}

			// GET
			GetRequestCyclingRecord param = new GetRequestCyclingRecord(url);
			AsyncTask<GetRequestCyclingRecord, Void, String> getTask
					= new CyclingRecordGetTask().execute(param);
			String jsonBody = getTask.get();
			System.out.println(jsonBody);
		} catch(Exception e) {
			Log.e("HTTP", e.getMessage());
		} finally {
		}
	}

	public void createUser(String url, String userId, String password) {
		try {
			URL testUrl = new URL("http://" + url + "/account");

			// パスワードを暗号化
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(password.getBytes());
			byte[] cipher = messageDigest.digest();
			StringBuilder sb = new StringBuilder();
			for (byte b : cipher) {
				sb.append(String.format("%02x", (b & 0xff)));
			}
//			Log.v("CIPHER", sb.toString());

			// サーバーに送信
			PostRequestUserCreate param = new PostRequestUserCreate(
					testUrl,
					userId,
					sb.toString()
			);
			AsyncTask<PostRequestUserCreate, Void, String> postTask
					= new UserCreatePostTask().execute(param);

			if (postTask != null) {
				// 受信データ確認
				Log.v("CRE_USER", postTask.get());
			}
		} catch (Exception e) {
			Log.e("CRE_USER", e.getMessage());
		}
	}
}
