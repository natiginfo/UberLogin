# UberLogin
Uber Login SDK - light version

## How to install?
### Step 1:
Add it in your root build.gradle at the end of repositories:

```
  allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

### Step 2:
Step 2. Add the dependency

```
	dependencies {
	        compile 'com.github.natiginfo:UberLogin:v1.0'
	}
```

### Step 3:
Step 3: Use inside activity:

``` 
UberLogin uberLogin = new UberLogin(this) {
            @Override
            public void onGetToken(String response) {
                Log.d("Uber Login Success", response);
            }

            @Override
            public void onGetError(String response) {
                Log.d("Uber Login Error", response);
            }
        };

uberLogin.setClientId("UBER_CLIENT_ID");
uberLogin.setClientSecret("UBER_CLIENT_SECRET");
uberLogin.setServerToken("UBER_SERVER_TOKEN");
```
