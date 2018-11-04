# AllergyWatch

**Udacity Android Developer Nanodegree final project, Capstone 1 and 2**

## Project Overview
AllergyWatch helps people with food allergy to decide whether they can eat a certain
food or not via scanning its barcode and showing contained ingredients and allergens.

## Tech stack
* Android Camera2 API for image capturing
* Firebase ML Vision API for barcode detecion
* Room persistence library for data storage
* MVVM design pattern using Android ViewModel with LiveData objects
* Master/Detail Flow with Fragments
* Retrofit for Web API connection and Gson for Json conversion
* Home screen widget

## Instruction Notes
### Signing configuration
App is signed with a custom signing configuration based on the settings in the app level 
`build.gradle` file. Signing keys can be found in `/udakeystore.jks` file.

### Test data
The App starts with 2 initial Products saved into the database that will be updated within 1 minute
when the system recognizes them as outdated. Considering the "single source of truth" pattern
data is always shown from the database and regularly updated through the network. This feature
can be disabled via commenting out the `MyDatabase.insertInitialData()` method.