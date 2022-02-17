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

package org.w3.banana.prefix

import org.w3.banana.{Ops, PrefixBuilder, RDF}

object IANALink:
   def apply[T <: RDF](using Ops[T]) = new IANALink()

/** The Iana Link Relations are not linked data so these URLs are currently invented ones, and need
  * not reflect what may be used if ever such URIs are coined.
  */
class IANALink[T <: RDF](using Ops[T])
    extends PrefixBuilder[T](
      "link",
      "http://www.iana.org/assignments/link-relations/#"
    ):
   val about               = apply("about")
   val acl                 = apply("acl")  // needs to be registered
   val alternate           = apply("alternate")
   val appendix            = apply("appendix")
   val archives            = apply("archives")
   val author              = apply("author")
   val bookmark            = apply("bookmark")
   val canonical           = apply("canonical")
   val chapter             = apply("chapter")
   val collection          = apply("collection")
   val contents            = apply("contents")
   val copyright           = apply("copyright")
   val create_form         = apply("create-form")
   val current             = apply("current")
   val describedby         = apply("describedby")
   val describes           = apply("describes")
   val disclosure          = apply("disclosure")
   val duplicate           = apply("duplicate")
   val edit                = apply("edit")
   val edit_form           = apply("edit-form")
   val edit_media          = apply("edit-media")
   val enclosure           = apply("enclosure")
   val first               = apply("first")
   val glossary            = apply("glossary")
   val help                = apply("help")
   val hosts               = apply("hosts")
   val hub                 = apply("hub")
   val icon                = apply("icon")
   val index               = apply("index")
   val item                = apply("item")
   val last                = apply("last")
   val latest_version      = apply("latest-version")
   val license             = apply("license")
   val lrdd                = apply("lrdd")
   val meta                = apply("meta") // needs to be registered
   val monitor             = apply("monitor")
   val monitor_group       = apply("monitor-group")
   val next                = apply("next")
   val next_archive        = apply("next-archive")
   val nofollow            = apply("nofollow")
   val noreferrer          = apply("noreferrer")
   val payment             = apply("payment")
   val predecessor_version = apply("predecessor-version")
   val prefetch            = apply("prefetch")
   val prev                = apply("prev")
   val prev_archive        = apply("prev-archive")
   val preview             = apply("preview")
   val previous            = apply("previous")
   val privacy_policy      = apply("privacy-policy")
   val profile             = apply("profile")
   val related             = apply("related")
   val replies             = apply("replies")
   val search              = apply("search")
   val section             = apply("section")
   val self                = apply("self")
   val service             = apply("service")
   val start               = apply("start")
   val stylesheet          = apply("stylesheet")
   val subsection          = apply("subsection")
   val successor_version   = apply("successor-version")
   val tag                 = apply("tag")
   val terms_of_service    = apply("terms-of-service")
   val tpe                 = apply("type")
   val up                  = apply("up")
   val version_history     = apply("version-history")
   val via                 = apply("via")
   val working_copy        = apply("working-copy")
   val working_copy_of     = apply("working-copy-of")
end IANALink
