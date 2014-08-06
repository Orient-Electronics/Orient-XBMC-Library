Orient XBMC Library
=========

This library replicates XBMC functionality to scrape media details.

Scraper
----
The scraper functionality is implemented using XBMC scrapers. The sscraper directory has to be specified first. The scraper directory expects to have sub-directories containing XBMC scrapers, with each directory representing one scraper. The directory name is treated as the scraper ID.

```java
Settings settings = Settings.getInstance();
settings.setAddonDir("assets/xbmc-addons/addons");
```

[Addon Directory Example Folder structure] (assets/xbmc-addons/addons)

**To Find a Movie:**

```java
String filename = "Mission Impossible Ghost Protocol [2011] 720p BRRip [Dual Audio] [English + Hindi] x264 BUZZccd [WBRG].mkv";

Scraper scraper = new Scraper("metadata.themoviedb.org");
ArrayList<ScraperUrl> movieResults = scraper.findMovie(filename, true);
```

This method returns an array of ScraperUrl objects. It does so by doing the following:

 - Extracts movie title and year (if available) from the given filename. (may be moved some where else, as XBMC is not consistant on this for different media types)
 - Uses scraper to find search results.
 - Calculates relevance score based on search terms (title and year)
 - Sorts based relevance score.

 

**To Find a Music Video:**

```java
String filename = "Michael Jackson - Beat It.avi";

Scraper scraper = new Scraper("metadata.musicvideos.theaudiodb.com");
ArrayList<ScraperUrl> musicVideoResults = scraper.findMovie(filename, true);
```
Works same way as the movie method, and even uses the same functions (the function may be given a generic name in future).
