/* tslint:disable */
/* eslint-disable */
/* Code generated by ng-openapi-gen DO NOT EDIT. */

export interface LazyLoadingEventRequest {

  /**
   * Index of the first record to fetch
   */
  first: number;

  /**
   * Global filter text to apply
   */
  globalFilter?: string;

  /**
   * Number of records per page
   */
  rows: number;

  /**
   * Field used for sorting
   */
  sortField?: string;

  /**
   * Sorting order, 1 for ascending, 0 for descending
   */
  sortOrder?: number;
}
