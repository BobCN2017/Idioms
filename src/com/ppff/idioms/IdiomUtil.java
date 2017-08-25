package com.ppff.idioms;

import com.github.promeg.pinyinhelper.Pinyin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;

/**
 * Created by PP on 2017/7/22.
 */

public class IdiomUtil {
	private static final String TAG = "IdiomUtil";
	private static Map<String, String> idiomMap;
	private static Set<Integer> allFourWordSet;
	private static Map<Long, UserInfo> userInfoMap = new HashMap<Long, UserInfo>();

	public static void init(InputStream idioms, InputStream fourwords) {

		if (idiomMap == null) {
			idiomMap = getIdiomMap(idioms);
		}
		if (allFourWordSet == null)
			allFourWordSet = getAllFourWordSet(fourwords);

		doPeriodRomoveInvalidDataTask();
	}

	public static String concatenateDragon(String text, long userId) {
		String result;
		if (text.contains("接龙")) {
			result = enterConcatenateDragon(text, userId);
		} else if (isRequestExit(text)) {
			result = exitConcatenateDragon(userId);
		} else {
			result = continueConcatenateDragon(text, userId);
		}

		return result;
	}

	private static boolean isRequestExit(String text) {

		return text.contains("退出") || text.contains("结束")
				|| text.contains("不玩");
	}

	private static String enterConcatenateDragon(String text, long userId) {
		String result = "进入成语接龙模式：";
		String subText = text.substring(text.indexOf("接龙") + 2);
		String chinese = getAndTrimChineseString(subText);
		String resultIdiom;
		if (chinese != null && isIdiom(chinese)) {
			resultIdiom = getIdiom(chinese.charAt(chinese.length() - 1));
		} else {
			resultIdiom = getRandomIdiom();
		}
		result += resultIdiom;

		if (stringIsEmpty(resultIdiom))
			result = "被你打败，进入接龙模式失败，退出接龙。";
		saveUsersData(userId, resultIdiom);

		return result;
	}

	private static void saveUsersData(long userId, String lastIdiom) {
		if (userInfoMap.containsKey(userId)) {
			UserInfo userInfo = userInfoMap.get(userId);
			userInfo.lastIdiom = lastIdiom;
			userInfo.lastTurnIsRight = true;
			userInfo.updateTime = System.currentTimeMillis();
		} else {
			userInfoMap.put(userId, new UserInfo(userId, lastIdiom));
		}
	}

	private static boolean isIdiom(String chinese) {
		if (chinese.length() == 4
				&& allFourWordSet.contains(chinese.hashCode()))
			return true;
		return false;
	}

	private static String exitConcatenateDragon(long userId) {
		removeUsersDate(userId);
		return "退出接龙模式，欢迎下次找我玩。";
	}

	public static void removeUsersDate(long userId) {
		if (userInfoMap.containsKey(userId))
			userInfoMap.remove(userId);
	}

	private static String continueConcatenateDragon(String userText, long userId) {
		String resultIdiom;
		if (checkConcatenationIsSuccess(userText, userId)) {
			String userIdiom = getAndTrimChineseString(userText);
			int lastIndex = userIdiom.length() - 1;
			resultIdiom = getIdiom(userIdiom.charAt(lastIndex));
			saveUsersData(userId, resultIdiom);
			return resultIdiom;
		} else if (lastTurnIsRight(userId)) {
			userInfoMap.get(userId).lastTurnIsRight = false;
			return "你接错了，请再来一次。";
		} else {
			removeUsersDate(userId);
			return "你接错了，退出接龙模式!";
		}

	}

	private static Boolean lastTurnIsRight(long userId) {
		return userInfoMap.containsKey(userId)
				&& userInfoMap.get(userId).lastTurnIsRight;
	}

	private static String getRandomIdiom() {
		Set<String> keys = idiomMap.keySet();
		String result = null;
		for (String key : keys) {
			if (Math.random() < 0.05)
				result = getIdiom('一', key);
		}
		return result;
	}

	private static boolean checkConcatenationIsSuccess(String text, long userId) {
		String chinese = getAndTrimChineseString(text);
		String lastIdiom = getLastIdiom(userId);
		if (stringIsEmpty(chinese) || stringIsEmpty(lastIdiom))
			return false;
		char last = lastIdiom.charAt(lastIdiom.length() - 1);
		char first = chinese.charAt(0);

		if (pinyinIsSame(last, first) && isIdiom(chinese))
			return true;

		return false;
	}

	private static boolean pinyinIsSame(char last, char first) {
		return Pinyin.toPinyin(last).equals(Pinyin.toPinyin(first));
	}

	private static String getLastIdiom(long userId) {
		if (userInfoMap.containsKey(userId))
			return userInfoMap.get(userId).lastIdiom;
		return null;
	}

	private static boolean stringIsEmpty(String string) {
		return string == null || string.isEmpty();
	}

	private static String getAndTrimChineseString(String text) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			if (Pinyin.isChinese(text.charAt(i))) {
				stringBuilder.append(text.charAt(i));
			}
		}

		return stringBuilder.toString();
	}

	private static String getIdiom(char chinese) {

		String pinyin = Pinyin.toPinyin(chinese);
		if (!Pinyin.isChinese(chinese) || !idiomMap.containsKey(pinyin))
			return null;

		return getIdiom(chinese, pinyin);
	}

	private static String getIdiom(char chinese, String pinyin) {
		String[] strArray = idiomMap.get(pinyin).split(" ");
		int index = getIndexOfFirstCharSameTo(chinese, strArray);
		if (index == -1)
			index = randomInt(strArray.length);
		return strArray[index];
	}

	private static int getIndexOfFirstCharSameTo(char chinese, String[] strArray) {

		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < strArray.length; i++) {
			if (chinese == strArray[i].charAt(0))
				list.add(i);
		}
		if (list.size() > 0) {
			int pos = randomInt(list.size());
			return list.get(pos);
		}
		return -1;
	}

	private static int randomInt(int number) {
		int index = (int) (Math.random() * number);
		return index == number ? index - 1 : index;
	}

	private static Map<String, String> getIdiomMap(InputStream idioms) {
		Map<String, String> idiomMap = new HashMap<String, String>();

		try {

			BufferedReader br = new BufferedReader(new InputStreamReader(
					idioms, "UTF-8"));
			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.isEmpty())
					continue;
				String pinyin = Pinyin.toPinyin(line.trim().charAt(0));
				if (idiomMap.get(pinyin) != null) {
					String str = idiomMap.get(pinyin);
					idiomMap.put(pinyin, str + " " + line.trim());
				} else {
					idiomMap.put(pinyin, line.trim());
				}
			}
			br.close();
			idioms.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return idiomMap;
	}

	private static Set<Integer> getAllFourWordSet(InputStream fourWords) {
		Set<Integer> set = new HashSet<Integer>();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fourWords, "UTF-8"));

			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.isEmpty())
					continue;
				set.add(line.hashCode());

			}
			br.close();
			fourWords.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return set;
	}
	
    private static void doPeriodRomoveInvalidDataTask(){
    	Timer timer=new Timer();
    	TimerTask task=new TimerTask() {			
			@Override
			public void run() {				
				removeInvalidUserData();
			}
		};
		timer.schedule(task, getTaskTime());
    }
    

	private static void removeInvalidUserData() {
		Set<Long> keys = userInfoMap.keySet();
//		System.out.println("userInfoMap size before clean:"+userInfoMap.size());
		for (Long key : keys) {
			UserInfo info = userInfoMap.get(key);
			Long diff = System.currentTimeMillis() - info.updateTime;
			if (diff > 24*60*60*1000)
				userInfoMap.remove(key);
		}
//		System.out.println("userInfoMap size after clean:"+userInfoMap.size());
	}
	
    private static Date getTaskTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 03);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND, 00);
        Date time = calendar.getTime();

        return time;
    }
    


	static class UserInfo {
		long userId;
		String lastIdiom;
		boolean lastTurnIsRight;
		long updateTime;

		public UserInfo(long id, String idiom) {
			userId = id;
			lastIdiom = idiom;
			lastTurnIsRight = true;
			updateTime = System.currentTimeMillis();
		}
	}

}
