# endless-recycler-view

## Overview

`EndlessRecyclerView` lets you to load new pages when a user scrolls down to the bottom of a list.
It extends `RecyclerView` and it is fully configurable to meet different development needs.

The library compatible with Android 14+.

## Usage

### Dependencies

To use the library in your project write this code to your build.gradle:

```groovy
buildscript {
    repositories {
        jcenter()
    }
}

dependencies {
    implementation 'com.github.yasevich:endless-recycler-view:2.0.0'
}
```

Or include it to your lib folder.

[![Download](https://api.bintray.com/packages/slava/maven/endless-recycler-view/images/download.svg)](https://bintray.com/slava/maven/endless-recycler-view/_latestVersion)

### Layout

You can include `EndlessRecyclerView` to your layout as following:

```xml
<com.github.yasevich.endlessrecyclerview.EndlessRecyclerView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

### Initialization

Set up `EndlessRecyclerView` parameters as required:

```java
EndlessRecyclerView list = ...; // initialization
list.setLayoutManager(new LinearLayoutManager(this));
list.setProgressView(R.layout.item_progress);
list.setAdapter(adapter);
list.setPager(this);
```

### Also

When page is loaded you may want to stop showing progress view:

```java
list.setRefreshing(false);
```

See sample and documentation for more details.
