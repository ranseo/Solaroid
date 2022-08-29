const functions = require("firebase-functions");
const axios = require('axios').default;
const async = require('async');
const admin = require("firebase-admin");

const serviceAccount = require("C:/Users/aa/Desktop/solaroid/service_account_key/service-account.json")
const adminConfig = JSON.parse(process.env.FIREBASE_CONFIG);

console.log(process.env.FIREBASE_CONFIG)
console.log(serviceAccount)
adminConfig.credential = admin.credential.cert(serviceAccount)

admin.initializeApp(adminConfig);

const kakaoRequestMeUrl = 'https://kapi.kakao.com/v2/user/me?secure_resource=true';

exports.kakaoToken = functions.region('asia-northeast3').https.onCall((data) => {
	console.log("KaKaoToken")
	console.log(data['access_token'])
	var access_token = data['access_token'];
	var token = createFirebaseToken(access_token);
	console.log(token)
	return token;
});

/**
 * requestMe - Returns user profile from Kakao API
 *
 * @param  {String} kakaoAccessToken Access token retrieved by Kakao Login API
 * @return {Promiise<Response>}      User profile response in a promise
 */
async function requestMe(kakaoAccessToken) {
	console.log('Requesting user profile from Kakao API server. ' + kakaoAccessToken);
	var result = await axios.get(kakaoRequestMeUrl, {
		method: 'GET',
		headers: { Authorization: 'Bearer ' + kakaoAccessToken },
	});
	return result;
}

async function updateOrCreateUser(updateParams) {
	console.log('updating or creating a firebase user');
	console.log(updateParams);
	try {
		// var userRecord = await admin.auth().getUserByEmail(updateParams['email']);
		var userRecord = await admin.auth().updateUser(updateParams['uid'], updateParams);
	} catch (error) {
		if (error.code === 'auth/user-not-found') {
			console.log(updateParams)
			return admin.auth().createUser(updateParams);
		}
		throw error;
	}

	return userRecord;
}

async function createFirebaseToken(kakaoAccessToken) {
  console.log("createFirebaseToken")
	var requestMeResult = await requestMe(kakaoAccessToken);
	const userData = requestMeResult.data; // JSON.parse(response)
	console.log(userData);

	const userId = `kakao:${userData.id}`;
	if (!userId) {
		return response.status(404).send({ message: 'There was no user with the given access token.' });
	}

	let nickname = null;
	let profileImage = null;
	if (userData.properties) {
		nickname = userData.properties.nickname;
		profileImage = userData.properties.profile_image;
	}


	//! Firebase 특성상 email 필드는 필수이다.
	//! 사업자등록 이후 email을 필수옵션으로 설정할 수 있으니 (카카오 개발자 사이트) 꼭 설정하자.
	//! 테스트 단계에서는 email을 동의하지 않고 로그인 할 경우 에러가 발생한다.
	const updateParams = {
		uid: userId,
		provider: 'KAKAO',
		displayName: nickname,
		email: userData.kakao_account.email,
		emailVerified : true
	};

	if (nickname) {
		updateParams['displayName'] = nickname;
	} else {
		updateParams['displayName'] = userData.kakao_account.email;
	}
	if (profileImage) {
		updateParams['photoURL'] = profileImage;
	}

	console.log(updateParams);

	var userRecord = await updateOrCreateUser(updateParams);

	return admin.auth().createCustomToken(userId, { provider: 'KAKAO' });
}
