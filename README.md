# endless-recycler-view

## Overview

`EndlessRecyclerView` lets you to load new pages when a user scrolls down to the bottom of a list. It extends `RecyclerView` and it is fully configurable to meet different development needs.

The library compatible with Android 7+.

## Usage

You can include `EndlessRecyclerView` to your layout as following:

```xml
<com.github.yasevich.endlessrecyclerview.EndlessRecyclerView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@android:id/list"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

Set up `EndlessRecyclerView` parameters as required:

```java
EndlessRecyclerView list = ...; // initialization
list.setLayoutManager(new LinearLayoutManager(this));
list.setProgressView(R.layout.item_progress);
list.setAdapter(adapter);
list.setPager(this);
```

See sample and documentation for more details.
