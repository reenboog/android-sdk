# FIDEL Android SDK

### Setup

Add [jitpack.io](https://www.jitpack.io) to your root build.gradle at the end of repositories:

```java
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
Add Fidel dependency

```java
dependencies {
    compile 'com.github.FidelLimited:android-sdk:-SNAPSHOT'
}
```
### Sample code

First, set up a programId and an apiKey (can be found in your dashboard):

```java
Fidel.programId = "your program id";
Fidel.apiKey = "your api key";
```
Then, present the Fidel activity:

```java
Fidel.present(YourActivityClass.this);
```

To automatically start credit card scanning, use:

```java
Fidel.autoScan = true;
```

You can retrieve a cardId, when a card is successfully added:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if(requestCode == Fidel.FIDEL_LINK_CARD_REQUEST_CODE) {
        if(data != null && data.hasExtra(Fidel.FIDEL_LINK_CARD_RESULT_CARD_ID)) {
            String cardId = data.getStringExtra(Fidel.FIDEL_LINK_CARD_RESULT_CARD_ID);

            Log.d("d", "CARD ID = " + cardId);
        }
    }
}
```
[![](https://jitpack.io/v/FidelLimited/android-sdk.svg)](https://jitpack.io/#FidelLimited/android-sdk)
