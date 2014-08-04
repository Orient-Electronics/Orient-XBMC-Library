Orient-XBMC-Library
===================


To Find a movie:

String filename = "Mission Impossible Ghost Protocol [2011] 720p BRRip [Dual Audio] [English + Hindi] x264 BUZZccd [WBRG].mkv";
Scraper scraper = new Scraper("metadata.themoviedb.org");
ArrayList<ScraperUrl> movieResults = scraper.FindMovie(filename, true);
