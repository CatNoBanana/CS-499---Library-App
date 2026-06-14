/*
* SortSearchManager
* Utility class used for sorting and filtering media collections
 */

package com.zybooks.judsonsinventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortSearchManager {


    //direction constants and sort fields
    public static final int SORT_TITLE = 0;
    public static final int SORT_YEAR = 1;
    public static final int SORT_RATING = 2;


    public static final int ORDER_ASC = 0;  //a->z, oldest, low->high
    public static final int ORDER_DESC = 1; //z->a, newest, high->low


    //hashmap category index
    //key = category name ("Books", "Movies", "Games")
    //value = list of items in that category

    //time complexity to build = O(n)

    public static Map<String, List<MediaItem>> buildCategoryIndex(List<MediaItem> allItems) {
        Map<String, List<MediaItem>> index = new HashMap<>();

        for (MediaItem item : allItems) {
            String key = item.getCategory();

            //if category hasn't been seen yet, create a new list for it
            if (!index.containsKey(key)) {
                index.put(key, new ArrayList<>());
            }

            index.get(key).add(item);
        }

        return index;
    }


    //retrieves all items for a given category from the index
    //returns an empty list if the category has no entries

    //time complexity = O(1)
    public static List<MediaItem> getFromIndex(Map<String, List<MediaItem>> index, String category) {
        if (index.containsKey(category)) {
            return new ArrayList<>(index.get(category));
        }

        return new ArrayList<>();
    }


    //Merge sort
    //adds support for sorting by title, year, or rating in ascending or descending order

    //time complexity = O(n log n)
    public static List<MediaItem> mergeSort(List<MediaItem> items, int sortField, int order) {
        if (items.size() <= 1) {
            return new ArrayList<>(items);
        }

        //divide the list in half
        int mid = items.size() / 2;
        List<MediaItem> left = mergeSort(items.subList(0, mid), sortField, order);
        List<MediaItem> right = mergeSort(items.subList(mid, items.size()), sortField, order);

        //merge the two sorted halves
        return merge(left, right, sortField, order);
    }


    //merge two sorted lists into one sorted list
    //time complexity = O(n)
    private static List<MediaItem> merge(List<MediaItem> left, List<MediaItem> right, int sortField, int order) {
        List<MediaItem> result = new ArrayList();
        int i = 0;
        int j = 0;

        //compare front elements of each half and take the smaller value if ascending, larger if descending
        while (i < left.size() && j < right.size()) {
            int cmp = compare(left.get(i), right.get(j), sortField);

            //for ascending, take left when left is <= right
            //for descending, take left when left >= right
            boolean takeLeft = (order == ORDER_ASC) ? (cmp <= 0) : (cmp >=0);

            if (takeLeft) {
                result.add(left.get(i++));
            }
            else {
                result.add(right.get(j++));
            }
        }

        //append any remaining elements from either half
        while (i < left.size()) {
            result.add(left.get(i++));
        }
        while (j < right.size()) {
            result.add(right.get(j++));
        }

        return result;
    }


    //compare two MediaItems by the specified sort field
    //returns negative if a < b, 0 if equal, positive if a > b
    private static int compare(MediaItem a, MediaItem b, int sortField) {
        switch (sortField) {
            case SORT_TITLE:
                //alphabetical comparison
                return a.getTitle().compareToIgnoreCase(b.getTitle());

            case SORT_YEAR:
                //earlier year = smaller value
                return Integer.compare(a.getYear(), b.getYear());

            case SORT_RATING:
                //lower rating = smaller value
                return Float.compare(a.getRating(), b.getRating());

            default:
                return 0;
        }
    }


    //Binary Search
    //used for fast title lookup on pre-sorted lists.

    //time complexity = O(log n) - halves the search space on every step
    public static List<MediaItem> binarySearch(List<MediaItem> sortedItems, String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(sortedItems);
        }

        String lowerQuery = query.toLowerCase().trim();
        List<MediaItem> results = new ArrayList<>();

        int low = 0;
        int high = sortedItems.size() - 1;
        int matchIndex = -1;

        //find any title that contains the query using binary search
        while (low <= high) {
            int mid = (low + high) / 2;
            String title = sortedItems.get(mid).getTitle().toLowerCase();

            if (title.contains(lowerQuery)) {
                matchIndex = mid;
                break;
            }
            else if (title.compareTo(lowerQuery) < 0) {
                low = mid + 1;
            }
            else {
                high = mid -1;
            }
        }

        //if no match found
        if (matchIndex == -1) {
            return results;
        }

        //expand left from matchIndex to find all matches
        int left = matchIndex - 1;
        while (left >= 0 && sortedItems.get(left).getTitle().toLowerCase().contains(lowerQuery)) {
            left--;
        }

        //expand right from matchIndex to find all matches
        int right = matchIndex + 1;
        while (right < sortedItems.size() && sortedItems.get(right).getTitle().toLowerCase().contains(lowerQuery)) {
            right++;
        }

        //gather results in the range
        for (int i = left + 1; i < right; i++) {
            results.add(sortedItems.get(i));
        }

        return results;
    }


    //back up linear search
    //used when lists are not sorted by title, possibly year or rating

    //time complexity = O(n)
    public static List<MediaItem> linearSearch(List<MediaItem> items, String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(items);
        }

        String lowerQuery = query.toLowerCase().trim();
        List<MediaItem> results = new ArrayList<>();

        for (MediaItem item : items) {
            if (item.getTitle().toLowerCase().contains(lowerQuery)) {
                results.add(item);
            }
        }

        return results;
    }


    //smart search
    //binary search used when the list is sorted by title
    public static List<MediaItem> search(List<MediaItem> items, String query, int sortField) {
        if (sortField == SORT_TITLE) {
            return binarySearch(items, query);
        }
        else {
            return linearSearch(items, query);
        }
    }
}
