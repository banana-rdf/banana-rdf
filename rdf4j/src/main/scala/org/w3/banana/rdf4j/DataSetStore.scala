/*
 *  Copyright (c) 2012 , 2021 W3C Members
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  This program and the accompanying materials are made available under
 *  the W3C Software Notice and Document License (2015-05-13) which is available at
 *  https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document.
 *
 *  SPDX-License-Identifier: W3C-20150513
 */

package org.w3.banana.rdf4j

import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.repository.Repository

/** An rdf4j Store is a set of `(subj, pred, obj, context)` Statements where `context` can be a null
  * value making it essentially equivalent to a `(subj, pred, obj)` Triple. This could be a good way
  * to model a DataSet.
  *
  * But, the problem is that `null` is also the symbol used by the wildcard, so that one cannot use
  * `ds.getStatements(null,null,null)` to search only for the statements in the default graph, since
  * that query is translated to `ds.getStatements(null,null,null,null)` and returns all the
  * statements in the set of quads. Ie. this would not be a problem if a different wildcard would
  * have been used, such as `*` for then one could distinguish `ds.getStatements(*,*,*,null)` to
  * return only the statements in the default graph, and `ds.getStatements(*,*,*,*)` to return all
  * the statements in the Store.
  *
  * As a result, if we want to be able to query the default graph, we need to name it. But what name
  * should one use?
  *   1. If one always used the same name in all datasets, then merging two DataSets would be
  *      problematic. 2. One could use a different name in each dataset (perhaps a blank node), but
  *      then how does the dataset know which one is the default?
  *
  *   1. would require having different policies for merging datasets, perhaps just giving the
  *      default graph the name of where it was found! Ie, the default graph is an indexical
  *
  * 2. The name that identifies the default has to be external to the dataset. One cannot place it
  * in the DS as otherwise an attack would be as easy as publishing some document at some location
  * L, stating that L is the default dataset. When we have a structure S with a point in the
  * structure this is known as a Pointed S. Here we would have a Pointed Store.
  *
  * One advantage of a Pointed Store is that by changing the point, one can change the perspective
  * of the store. The store would have a coherent view of the data, but different entry points.
  * (assuming the representations for each resource is identical for all). So perhaps the Fetcher
  * would use the store with the default pointing to the fetcher's graph, where it could keep all
  * the info of when it fetched graphs and what results there were. But the guard could have as
  * default the graph that told it where the data.
  *
  * Annoyingly we must choose to name all graphs, because keeping one unnamed is not an option in
  * rdf4j since it is not possible to search for the null graph.
  *
  * It is not clear that this is needed yet, but I want to keep the reasoning above.
  */
case class DataSetStore(point: Resource, quads: Repository)
