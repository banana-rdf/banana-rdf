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

import org.w3.banana.{RDF, Ops, PrefixBuilder}
import io.lemonlabs.uri.*

object FOAF:

   def apply[Rdf <: RDF](using ops: Ops[Rdf]) = new FOAF()

class FOAF[Rdf <: RDF](using ops: Ops[Rdf])
    extends PrefixBuilder[Rdf]("foaf", ops.URI("http://xmlns.com/foaf/0.1/")):
   // todo: remove these relations as they don't exist in foaf
   val height      = apply("height")
   val publication = apply("publication")
   val wants       = apply("wants")
   val author      = apply("author")

   // resources that actually exist in foaf
   val Agent                   = apply("Agent")
   val Document                = apply("Document")
   val Group                   = apply("Group")
   val Image                   = apply("Image")
   val LabelProperty           = apply("LabelProperty")
   val OnlineAccount           = apply("OnlineAccount")
   val OnlineChatAccount       = apply("OnlineChatAccount")
   val OnlineEcommerceAccount  = apply("OnlineEcommerceAccount")
   val OnlineGamingAccount     = apply("OnlineGamingAccount")
   val Organization            = apply("Organization")
   val Person                  = apply("Person")
   val PersonalProfileDocument = apply("PersonalProfileDocument")
   val Project                 = apply("Project")
   val account                 = apply("account")
   val accountName             = apply("accountName")
   val accountServiceHomepage  = apply("accountServiceHomepage")
   val age                     = apply("age")
   val aimChatID               = apply("aimChatID")
   val based_near              = apply("based_near")
   val birthday                = apply("birthday")
   val currentProject          = apply("currentProject")
   val depiction               = apply("depiction")
   val depicts                 = apply("depicts")
   val dnaChecksum             = apply("dnaChecksum")
   val familyName              = apply("familyName")
   val family_name             = apply("family_name")
   val firstName               = apply("firstName")
   val focus                   = apply("focus")
   val fundedBy                = apply("fundedBy")
   val geekcode                = apply("geekcode")
   val gender                  = apply("gender")
   val givenName               = apply("givenName")
   val givenname               = apply("givenname")
   val holdsAccount            = apply("holdsAccount")
   val homepage                = apply("homepage")
   val icqChatID               = apply("icqChatID")
   val img                     = apply("img")
   val interest                = apply("interest")
   val isPrimaryTopicOf        = apply("isPrimaryTopicOf")
   val jabberID                = apply("jabberID")
   val knows                   = apply("knows")
   val lastName                = apply("lastName")
   val logo                    = apply("logo")
   val made                    = apply("made")
   val maker                   = apply("maker")
   val mbox                    = apply("mbox")
   val mbox_sha1sum            = apply("mbox_sha1sum")
   val member                  = apply("member")
   val membershipClass         = apply("membershipClass")
   val msnChatID               = apply("msnChatID")
   val myersBriggs             = apply("myersBriggs")
   val name                    = apply("name")
   val nick                    = apply("nick")
   val openid                  = apply("openid")
   val page                    = apply("page")
   val pastProject             = apply("pastProject")
   val phone                   = apply("phone")
   val plan                    = apply("plan")
   val primaryTopic            = apply("primaryTopic")
   val publications            = apply("publications")
   val schoolHomepage          = apply("schoolHomepage")
   val sha1                    = apply("sha1")
   val skypeID                 = apply("skypeID")
   val status                  = apply("status")
   val surname                 = apply("surname")
   val theme                   = apply("theme")
   val thumbnail               = apply("thumbnail")
   val tipjar                  = apply("tipjar")
   val title                   = apply("title")
   val topic                   = apply("topic")
   val topic_interest          = apply("topic_interest")
   val weblog                  = apply("weblog")
   val workInfoHomepage        = apply("workInfoHomepage")
   val workplaceHomepage       = apply("workplaceHomepage")
   val yahooChatID             = apply("yahooChatID")
end FOAF
