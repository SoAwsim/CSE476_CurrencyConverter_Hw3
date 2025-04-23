# Yeditepe University CSE 476 Currency Converter Assignment 3

In this assignment we were expected to implement a simple currency converter using the https://currencyfreaks.com/
API. The goal of this assignment was to implement web API calls in Android which returns a XML response.

I have tried to implement MVC architecture using Android ViewModel to separate business logic from the UI
logic. This way I do not have manually save and restore components like I did in the previous assignment
since ViewModels are tied to the application life cycle.

A down side of the application is that I convert everything to USD then the I convert to to my target
currency. While for actual currency conversions this implementation is not valid for my use case it
suits my needs since other API calls are available with the paid tier.

## Features
- Has Internet access and can query web services for exchange rates.
- Has easy to use layout with currency type selectors, two text boxes, one for
amount entry and one to show the calculation result - non-editable - and a
button that will trigger the conversion.
- Gives the user the ability to pick and choose among multiple currencies,
and can convert to and from them.
- Exchange rates could be retrieved from the Exchange web service
( https://currencyfreaks.com/documentation .html ), which also provides examples
on its use with REST XML and HTTP GET/POST methods.
- The app should quote foreign currency and crypto exchange rates from the
service, parse returned data and make the calculations accordingly. Hence,
the calculation result should be dynamic.
- Retrieves all available currencies dynamically from the API and gets image assets from the API. Also
images are cached into the application storage for the next time the application is used.

## Running
You need an API key from https://currencyfreaks.com/. This API key should be stored in the apikey.properties
file which is generated during the configuration phase of the build.gradle script. The template for this
file is available in the apikey.properties.template file which the gradle script is going to generate a apikey.properties
from the content of this file. Once the file is generated you can write your API key into this file.

After the file is generated put your API key into the apikey.properties file as that is the file which
the build script reads from. It is also ignored by git therefore you won't be able accidentally push
this file by default.

Keep in mind that Android Studio might hide other source file since the default file layout is set as
Android, you need to change that to the Project to see all of the files under your project's directory.
You might also need to reload files from the disk after the initialization finishes since by default
apikey.properties might not show up in the Android Studio.

Clone the repo:
```bash
git clone https://github.com/SoAwsim/CSE476_CurrencyConverter_Hw3
```

Put your API key to the apikey.properties
```bash
CURRENCY_API_KEY=YOUR_API_KEY
```

Then build using Android Studio.
