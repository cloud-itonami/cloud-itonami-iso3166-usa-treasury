(ns statute.facts
  "Agency-level compliance catalog for **USA-TREASURY** (United States
  Department of the Treasury) -- the spec-basis behind this leaf's blueprint
  claim that an independent operator can run a Treasury/IRS registration and
  tax-compliance navigation service.

  Scope. This is the Treasury-specific layer only. Government-wide U.S. federal
  statutes live in the country coordinator `cloud-itonami-iso3166-usa`'s
  `statute.facts` and are NOT duplicated here; the two catalogs compose, keyed
  `USA-TREASURY` -> `USA`. Sibling agency leaves (`USA-DOT`, `USA-FCC`,
  `USA-SEC`, `USA-DOC`) hold their own chapters. Three government-wide bodies
  of rule ARE carried here anyway -- the FAR (48 CFR chapter 1), OMB's
  identifier rule (2 CFR part 25) and OMB's uniform guidance (2 CFR part 200)
  -- because every finding below is a statement about how Treasury relates to
  them, and a contrast needs both sides present to be checked.

  Provenance. Every entry cites the official eCFR (Electronic Code of Federal
  Regulations, GPO/Office of the Federal Register) address for the smallest
  stable unit that was independently confirmed. Nothing here is fabricated:
  each `:statute/verified-label` is the byte-exact `label_description` returned
  by the eCFR versioner structure API on `:statute/verified-at`, and each
  string in `:statute/verified-quotes` is a byte-exact span of the section text
  returned by the eCFR versioner full-text API. `tools/verify_citations.cljs`
  re-fetches both and fails if either drifts.

  Why the citation and the verification URL differ. `:statute/url` is the
  canonical human address a person should open. It is deliberately NOT the URL
  that was machine-verified: fetching www.ecfr.gov from an automated client can
  return HTTP 200 with a `Federal Register :: Request Access` interstitial
  rather than the regulation, so a status-code check against it would report
  success while proving nothing. We verify through the documented machine API
  and record both. The human URLs here were constructed from the same verified
  node paths rather than fetched -- do not `curl` one and treat a 200 as
  confirmation, because it is not.

  THE TRAP THIS CATALOG EXISTS TO PIN DOWN. **The Employer Identification
  Number is not a federal-award credential, and the activity of obtaining one
  for a client is itself a regulated practice whose rules are not in the tax
  title.** This leaf's own blueprint describes the product as an `EIN issuance
  pathway and federal tax-registration checklist for award eligibility`. Two
  independent halves of that sentence are refuted by the regulations below, and
  both refutations are recorded as data -- a quoted span or a checked negative
  -- rather than as prose, so that a reorganisation of the CFR cannot leave a
  stale claim sitting here looking verified:

  1. **Award eligibility does not run on the EIN.** 2 CFR 25.100(a) says the
     unique entity identifier `is the universal identifier for Federal
     financial assistance applicants`, and SAM.gov is the repository. Scanning
     the entire text of 2 CFR part 25 -- the part whose subject IS the
     identifier an applicant must hold -- for `taxpayer`, `TIN`, `EIN` or
     `employer identification` returns nothing. The same scan over 2 CFR part
     200, the whole of OMB's uniform guidance, also returns nothing. The
     taxpayer identifier is not a weak requirement in federal assistance; it is
     absent from the rulebook. An operator selling `get an EIN, then you are
     eligible` has sold a step that the governing regulation never asks for.

  2. **Obtaining an EIN for someone else is practice before the IRS.** 31 CFR
     10.2 defines that practice to comprehend `all matters connected with a
     presentation to the Internal Revenue Service`, expressly including
     `preparing documents; filing documents; corresponding and communicating
     with the Internal Revenue Service`. Filing a client's Form SS-4 and
     handling what comes back is squarely inside that definition. 31 CFR part
     10 -- Circular 230 -- then restricts who may do it. This is the operator's
     own licence question, and it is the one thing a catalog built out of title
     26 would never surface, because **the phrase `practice before` appears in
     no node label anywhere in title 26**. The rules live in title 31, under
     the Office of the Secretary, two titles away from the tax code they
     govern.

  Where the EIN does matter, and the distinction that is the actual product.
  On the CONTRACT side the taxpayer identifier is real: FAR subpart 4.9 is
  titled `Taxpayer Identification Number Information` and 48 CFR 52.204-3 is
  the solicitation provision that collects it. On the ASSISTANCE side it is
  absent, as above. So the first question for any Treasury dollar is the same
  one the DOT leaf found for transportation -- **which side of the money is
  this** -- and the answer changes which identifier even exists as a concept.
  That question, not the SS-4, is what an operator can actually sell.

  Two further hazards are carried by the entries rather than by `absences`,
  because in both cases the evidence is a heading that exists:

  * **A chapter heading is not a current statement of ownership.** 31 CFR
    chapter IV is labelled `Secret Service, Department of the Treasury` and
    chapter VII `Federal Law Enforcement Training Center, Department of the
    Treasury`; 31 CFR part 8 is `Practice Before the Bureau of Alcohol, Tobacco
    and Firearms`. All three carry live, unreserved text. The labels are the
    only organisational claim being made -- 31 CFR 700.2, for instance, defines
    FLETC's sites and never names a department. This is the mirror image of the
    hazard the DOT leaf recorded: there, non-DOT agencies sat in the
    transportation title and their own labels said so, and the error was to
    assume the title implied the owner. Here the labels affirmatively say
    `Department of the Treasury` for functions that no longer report to it, and
    the error is to believe them. In both directions the rule is the same: the
    heading tells you where the text is filed, not who runs it today.

  * **Treasury's reach is not confined to the Treasury title.** 12 CFR chapter
    I is `Comptroller of the Currency, Department of the Treasury` -- a Treasury
    bureau whose entire rulebook is in Banks and Banking. An operator who
    equates `Treasury regulation` with `31 CFR` has excluded the bureau that
    regulates national banks, along with the tax rules in 26 CFR and the
    acquisition rules in 48 CFR. Five titles carry this department's rules,
    which is itself the finding.

  What is genuinely Treasury-only. Three things a client cannot get from a
  generic federal-compliance adviser: (a) knowing which of Treasury's hats a
  given obligation comes from -- acquirer, grantor, tax administrator, practice
  regulator, or financial regulator -- because the last of these binds parties
  who have no relationship with Treasury at all; (b) the beneficial-ownership
  position, where 31 CFR 1010.380(c)(1)(i) is now `[Reserved]` and the
  reporting company definition reaches only entities `Formed under the law of a
  foreign country`, so the advice most commonly given to a domestic LLC is
  currently advice to make a filing the rule does not require; and (c) the
  recordkeeping asymmetry, where OFAC's 31 CFR 501.601 obliges `every person`
  to retain transaction records `for at least 10 years` -- a duty owed to a
  Treasury bureau by parties who are neither its contractors nor its
  grantees."
  (:require [clojure.string :as str]))

;; ---------------------------------------------------------------------------
;; Verification endpoints.
;;
;; Pinned to a dated snapshot rather than `current` so that a run is
;; reproducible: `current` would silently change the thing being compared
;; against, which is the failure mode where a gate keeps passing because both
;; sides moved together.
;;
;; Five titles, which is itself the finding. A department whose rules could be
;; read in one place would need one.

(def ecfr-structure-api
  "CFR title -> eCFR versioner *structure* endpoint. Yields the node tree whose
  `label_description` fields the positive half of the gate compares against."
  {2  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-2.json"
   12 "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-12.json"
   26 "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-26.json"
   31 "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
   48 "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"})

(def ecfr-full-text-api
  "CFR title -> eCFR versioner *full-text* endpoint. A quote check appends
  `?part=<part>&section=<section>`; a text-absence check appends `?part=<part>`
  alone. Declared per title rather than built from a prefix so that an entry
  citing a title nobody declared an endpoint for is a could-not-answer instead
  of a fetch against a URL nobody checked. Title 12 is deliberately absent: the
  OCC entry below is heading-only, and an endpoint declared for a title no
  quote uses would be dead configuration that looks like coverage."
  {2  "https://www.ecfr.gov/api/versioner/v1/full/2026-08-18/title-2.xml"
   26 "https://www.ecfr.gov/api/versioner/v1/full/2026-08-18/title-26.xml"
   31 "https://www.ecfr.gov/api/versioner/v1/full/2026-08-18/title-31.xml"
   48 "https://www.ecfr.gov/api/versioner/v1/full/2026-08-18/title-48.xml"})

;; ---------------------------------------------------------------------------
;; The catalog.
;;
;; `USA-TREASURY` is an agency-level key (parent `USA`), matching
;; blueprint.edn's `:itonami.blueprint/iso3166`.
;;
;; `:statute/cfr-node` is the path from the CFR title down to the cited node,
;; as [type identifier] pairs. The live gate walks the eCFR structure tree by
;; this path -- it does not string-match the URL, because hierarchical
;; identifiers nest as substrings of one another (part `1` is a prefix of part
;; `1010`, part `25` of part `2501`, and section `10.3` of `10.30`). Walking
;; explicit [type identifier] steps cannot pass by accident.
;;
;; A step may be `"*"`, used ONLY where eCFR generates the identifier rather
;; than the CFR citing it -- `subpart` and `subject_group` nodes inside 26 CFR
;; part 301 have identifiers like `ECFR1b5d05d4bfe19f9`, which are not citable
;; addresses and are not stable enough to record. A wildcard that resolves to
;; more than one node is reported as a failure, not silently taken.
;;
;; `:statute/hat` says which Treasury role the entry belongs to. Conflating
;; these is the failure this catalog exists to prevent, so it is a required
;; field:
;;   :acquirer             -- Treasury buying for itself, under the FAR as
;;                            supplemented by the DTAR (48 CFR chapter 10)
;;   :grantor              -- Treasury awarding financial assistance, where the
;;                            procurement being regulated belongs to the
;;                            recipient
;;   :tax-administrator    -- the IRS administering the internal revenue laws
;;                            (26 CFR chapter I)
;;   :practice-regulator   -- Treasury regulating who may represent taxpayers
;;                            before the IRS (31 CFR part 10, Circular 230).
;;                            This hat regulates the OPERATOR, not the client
;;   :financial-regulator  -- Treasury bureaus writing rules that bind parties
;;                            with no Treasury relationship at all (OFAC,
;;                            FinCEN, CFIUS)
;;   :elsewhere            -- a Treasury bureau whose rules are outside 31 CFR
;;   :stale-label          -- a heading naming Treasury for a function that no
;;                            longer reports to it
;;   :far-baseline         -- 48 CFR chapter 1: what governs before a supplement
;;   :assistance-baseline  -- 2 CFR: OMB's government-wide assistance rules

(def catalog
  "USA-TREASURY -> ordered vector of verified regulatory anchors."
  {"USA-TREASURY"
   [
    ;; -----------------------------------------------------------------------
    ;; Baseline: what governs before Treasury says anything.

    {:statute/id            :far/root
     :statute/topic         #{:procurement}
     :statute/hat           :far-baseline
     :statute/title         "48 CFR Chapter 1 -- Federal Acquisition Regulation"
     :statute/cfr-title     48
     :statute/cfr-node      [["chapter" "1"]]
     :statute/url           "https://www.ecfr.gov/current/title-48/chapter-1"
     :statute/verified-label "Federal Acquisition Regulation"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "What governs selling to any federal agency before a supplement is
      consulted. Treasury does publish a supplement (chapter 10), but it is
      thin by design -- see :dtar/purpose, which says so in the regulation's
      own words."}

    {:statute/id            :far/taxpayer-identification
     :statute/topic         #{:procurement :identifier}
     :statute/hat           :far-baseline
     :statute/title         "48 CFR Subpart 4.9 -- Taxpayer Identification Number Information"
     :statute/cfr-title     48
     :statute/cfr-node      [["chapter" "1"] ["subchapter" "A"] ["part" "4"] ["subpart" "4.9"]]
     :statute/url           "https://www.ecfr.gov/current/title-48/chapter-1/subchapter-A/part-4/subpart-4.9"
     :statute/verified-label "Taxpayer Identification Number Information"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The counterweight to this catalog's central finding, and the reason that
      finding is a distinction rather than a debunking. On the CONTRACT side the
      taxpayer identifier genuinely is collected: the FAR devotes a subpart to
      it. On the ASSISTANCE side it is absent from the entire rulebook -- see
      the absence :treasury/no-taxpayer-identifier-in-assistance-rules. Same
      federal government, same firm, two regimes, and the identifier that
      matters is not the same one. Note also that DoD supplemented this subpart
      at 48 CFR 204.9 and DOT at 1204.9; Treasury has no part 1004 at all."}

    {:statute/id            :far/tin-provision
     :statute/topic         #{:procurement :identifier}
     :statute/hat           :far-baseline
     :statute/title         "48 CFR 52.204-3 -- Taxpayer Identification"
     :statute/cfr-title     48
     :statute/cfr-node      [["chapter" "1"] ["subchapter" "H"] ["part" "52"] ["subpart" "52.2"] ["section" "52.204-3"]]
     :statute/url           "https://www.ecfr.gov/current/title-48/chapter-1/subchapter-H/part-52/subpart-52.2/section-52.204-3"
     :statute/verified-label "Taxpayer identification."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The actual solicitation provision that collects a TIN from an offeror.
      Recorded so that `the EIN is irrelevant` cannot be over-read from this
      catalog: it is irrelevant to assistance eligibility, and it is collected
      by name in contracting."}

    {:statute/id            :omb/assistance-subtitle
     :statute/topic         #{:assistance}
     :statute/hat           :assistance-baseline
     :statute/title         "2 CFR Subtitle A -- OMB Guidance for Federal Financial Assistance"
     :statute/cfr-title     2
     :statute/cfr-node      [["subtitle" "A"]]
     :statute/url           "https://www.ecfr.gov/current/title-2/subtitle-A"
     :statute/verified-label "Office of Management and Budget Guidance for Federal Financial Assistance"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-2.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The government-wide assistance rulebook. Treasury adopts it nearly whole
      (see :treasury/grants-adoption), which is why so little of what binds a
      Treasury grantee is Treasury text."}

    {:statute/id            :uei/part
     :statute/topic         #{:assistance :identifier}
     :statute/hat           :assistance-baseline
     :statute/title         "2 CFR Part 25 -- Unique Entity Identifier and System for Award Management"
     :statute/cfr-title     2
     :statute/cfr-node      [["subtitle" "A"] ["chapter" "I"] ["part" "25"]]
     :statute/url           "https://www.ecfr.gov/current/title-2/subtitle-A/chapter-I/part-25"
     :statute/verified-label "Unique Entity Identifier and System for Award Management"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-2.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The part whose entire subject is the identifier a federal-assistance
      applicant must hold. It never mentions a taxpayer identifier. That is the
      checked negative this leaf is built on."}

    {:statute/id            :uei/purpose
     :statute/topic         #{:assistance :identifier}
     :statute/hat           :assistance-baseline
     :statute/title         "2 CFR 25.100 -- Purpose of this part"
     :statute/cfr-title     2
     :statute/cfr-node      [["subtitle" "A"] ["chapter" "I"] ["part" "25"] ["subpart" "A"] ["section" "25.100"]]
     :statute/url           "https://www.ecfr.gov/current/title-2/subtitle-A/chapter-I/part-25/subpart-A/section-25.100"
     :statute/verified-label "Purpose of this part."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-2.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "25"
     :statute/quote-section "25.100"
     :statute/verified-quotes
     ["The unique entity identifier (UEI) is the universal identifier for Federal financial assistance applicants"
      "The System for Award Management (SAM.gov) is the repository for standard information about applicants and recipients"]
     :statute/note
     "`Universal` is the regulation's own word, and it is attached to the UEI,
      not the EIN. Both spans are load-bearing: the first names the identifier,
      the second names the system a client must actually be registered in. An
      onboarding checklist that produces an EIN and stops has produced neither."}

    {:statute/id            :uei/applicability
     :statute/topic         #{:assistance :scope}
     :statute/hat           :assistance-baseline
     :statute/title         "2 CFR 25.105 -- Applicability"
     :statute/cfr-title     2
     :statute/cfr-node      [["subtitle" "A"] ["chapter" "I"] ["part" "25"] ["subpart" "A"] ["section" "25.105"]]
     :statute/url           "https://www.ecfr.gov/current/title-2/subtitle-A/chapter-I/part-25/subpart-A/section-25.105"
     :statute/verified-label "Applicability."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-2.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "25"
     :statute/quote-section "25.105"
     :statute/verified-quotes
     ["This part does not apply to subrecipients of subrecipients (second-tier subrecipients) or contractors under Federal awards."]
     :statute/note
     "The boundary of the UEI regime, and a second place the two sides of the
      money separate: a CONTRACTOR under a federal award is outside this part
      entirely. Advising a subcontractor to obtain a UEI, or a second-tier
      subrecipient to register in SAM.gov, is advising work the rule does not
      ask for."}

    {:statute/id            :omb/uniform-guidance
     :statute/topic         #{:assistance}
     :statute/hat           :assistance-baseline
     :statute/title         "2 CFR Part 200 -- Uniform Administrative Requirements, Cost Principles, and Audit Requirements for Federal Awards"
     :statute/cfr-title     2
     :statute/cfr-node      [["subtitle" "A"] ["chapter" "II"] ["part" "200"]]
     :statute/url           "https://www.ecfr.gov/current/title-2/subtitle-A/chapter-II/part-200"
     :statute/verified-label "Uniform Administrative Requirements, Cost Principles, and Audit Requirements for Federal Awards"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-2.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "Note the chapter: part 25 is in chapter I and part 200 in chapter II, so
      the two halves of OMB's assistance guidance are not even siblings. A
      reader who found one by browsing has not necessarily seen the other. The
      taxpayer identifier is absent from both."}

    ;; -----------------------------------------------------------------------
    ;; Hat 1: Treasury as acquirer. The DTAR.

    {:statute/id            :dtar/chapter
     :statute/topic         #{:procurement}
     :statute/hat           :acquirer
     :statute/title         "48 CFR Chapter 10 -- Department of the Treasury"
     :statute/cfr-title     48
     :statute/cfr-node      [["chapter" "10"]]
     :statute/url           "https://www.ecfr.gov/current/title-48/chapter-10"
     :statute/verified-label "Department of the Treasury"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "Treasury's FAR supplement. Unlike the FCC leaf's agency, Treasury has
      one; unlike DOT's, it carves out no operating administration. Its
      distinguishing feature is how little it says -- fourteen live parts
      against the FAR's fifty-three."}

    {:statute/id            :dtar/system
     :statute/topic         #{:procurement}
     :statute/hat           :acquirer
     :statute/title         "48 CFR Part 1001 -- Department of the Treasury Acquisition Regulation (DTAR) System"
     :statute/cfr-title     48
     :statute/cfr-node      [["chapter" "10"] ["subchapter" "A"] ["part" "1001"]]
     :statute/url           "https://www.ecfr.gov/current/title-48/chapter-10/subchapter-A/part-1001"
     :statute/verified-label "Department of the Treasury Acquisition Regulation (DTAR) System"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"
     :statute/verified-at   "2026-08-20"}

    {:statute/id            :dtar/purpose
     :statute/topic         #{:procurement :scope}
     :statute/hat           :acquirer
     :statute/title         "48 CFR 1001.101 -- Purpose"
     :statute/cfr-title     48
     :statute/cfr-node      [["chapter" "10"] ["subchapter" "A"] ["part" "1001"] ["subpart" "1001.1"] ["section" "1001.101"]]
     :statute/url           "https://www.ecfr.gov/current/title-48/chapter-10/subchapter-A/part-1001/subpart-1001.1/section-1001.101"
     :statute/verified-label "Purpose."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "1001"
     :statute/quote-section "1001.101"
     :statute/verified-quotes
     ["The DTAR contains policies and procedures that supplement FAR coverage"
      "When FAR coverage is adequate, there will be no corresponding DTAR coverage."]
     :statute/note
     "The second span is the regulation explaining its own thinness, and it is
      the most useful sentence in the DTAR for an adviser. It converts every
      gap in chapter 10 from an ambiguity into a positive statement: no DTAR
      part means the FAR governs unmodified. That is why the missing part 1004
      recorded in `absences` is evidence rather than an oddity."}

    {:statute/id            :dtar/definitions
     :statute/topic         #{:procurement}
     :statute/hat           :acquirer
     :statute/title         "48 CFR Part 1002 -- Definitions of Words and Terms"
     :statute/cfr-title     48
     :statute/cfr-node      [["chapter" "10"] ["subchapter" "A"] ["part" "1002"]]
     :statute/url           "https://www.ecfr.gov/current/title-48/chapter-10/subchapter-A/part-1002"
     :statute/verified-label "Definitions of Words and Terms"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"
     :statute/verified-at   "2026-08-20"}

    {:statute/id            :dtar/qualifications
     :statute/topic         #{:procurement :eligibility}
     :statute/hat           :acquirer
     :statute/title         "48 CFR Part 1009 -- Contractor Qualifications"
     :statute/cfr-title     48
     :statute/cfr-node      [["chapter" "10"] ["subchapter" "B"] ["part" "1009"]]
     :statute/url           "https://www.ecfr.gov/current/title-48/chapter-10/subchapter-B/part-1009"
     :statute/verified-label "Contractor Qualifications"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The DTAR part closest to `am I eligible to sell to Treasury`. Note what it
      is not: it is a responsibility and debarment part, not a registration
      part. Nothing in chapter 10 issues a credential."}

    {:statute/id            :dtar/small-business
     :statute/topic         #{:procurement :socioeconomic}
     :statute/hat           :acquirer
     :statute/title         "48 CFR Part 1019 -- Small Business Programs"
     :statute/cfr-title     48
     :statute/cfr-node      [["chapter" "10"] ["subchapter" "D"] ["part" "1019"]]
     :statute/url           "https://www.ecfr.gov/current/title-48/chapter-10/subchapter-D/part-1019"
     :statute/verified-label "Small Business Programs"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"
     :statute/verified-at   "2026-08-20"}

    {:statute/id            :dtar/omwi
     :statute/topic         #{:procurement :socioeconomic}
     :statute/hat           :acquirer
     :statute/title         "48 CFR Part 1022 -- Minority and Women Inclusion"
     :statute/cfr-title     48
     :statute/cfr-node      [["chapter" "10"] ["subchapter" "D"] ["part" "1022"]]
     :statute/url           "https://www.ecfr.gov/current/title-48/chapter-10/subchapter-D/part-1022"
     :statute/verified-label "Minority and Women Inclusion"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "Genuinely Treasury-distinctive, and one of only two socioeconomic parts in
      the whole DTAR. A supplier-diversity obligation that a firm selling to
      most other departments will not have met before, and one that does not
      appear in the FAR baseline at all."}

    ;; -----------------------------------------------------------------------
    ;; Hat 2: Treasury as grantor. Almost nothing, and that is the point.

    {:statute/id            :treasury/grants-chapter
     :statute/topic         #{:assistance}
     :statute/hat           :grantor
     :statute/title         "2 CFR Chapter X -- Department of Treasury"
     :statute/cfr-title     2
     :statute/cfr-node      [["subtitle" "B"] ["chapter" "X"]]
     :statute/url           "https://www.ecfr.gov/current/title-2/subtitle-B/chapter-X"
     :statute/verified-label "Department of Treasury"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-2.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "Treasury's entire agency-specific assistance regulation: one part, three
      sections. Recorded because the emptiness is the finding -- a department
      that has administered some of the largest assistance programmes in recent
      federal history writes almost no assistance rules of its own. Note also
      that the label omits `the`, unlike every other Treasury heading in this
      catalog; it is recorded byte-exactly rather than tidied."}

    {:statute/id            :treasury/grants-part
     :statute/topic         #{:assistance}
     :statute/hat           :grantor
     :statute/title         "2 CFR Part 1000 -- Uniform Administrative Requirements, Cost Principles, and Audit Requirements for Federal Awards"
     :statute/cfr-title     2
     :statute/cfr-node      [["subtitle" "B"] ["chapter" "X"] ["part" "1000"]]
     :statute/url           "https://www.ecfr.gov/current/title-2/subtitle-B/chapter-X/part-1000"
     :statute/verified-label "Uniform Administrative Requirements, Cost Principles, and Audit Requirements for Federal Awards"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-2.json"
     :statute/verified-at   "2026-08-20"}

    {:statute/id            :treasury/grants-adoption
     :statute/topic         #{:assistance :scope}
     :statute/hat           :grantor
     :statute/title         "2 CFR 1000.10 -- Applicable regulations"
     :statute/cfr-title     2
     :statute/cfr-node      [["subtitle" "B"] ["chapter" "X"] ["part" "1000"] ["section" "1000.10"]]
     :statute/url           "https://www.ecfr.gov/current/title-2/subtitle-B/chapter-X/part-1000/section-1000.10"
     :statute/verified-label "Applicable regulations."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-2.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "1000"
     :statute/quote-section "1000.10"
     :statute/verified-quotes
     ["the Department of the Treasury adopts the Uniform Administrative Requirements, Cost Principles, and Audit Requirements for Federal Awards, set forth at 2 CFR part 200"]
     :statute/note
     "The whole of Treasury's grants posture in one sentence: adopt 2 CFR 200,
      deviate only where stated. For an operator this is good news and it is
      checkable -- a Treasury grantee's obligations are almost entirely the
      government-wide ones, so expertise transfers between Treasury programmes
      and other agencies' alike. It also means an adviser who has memorised
      chapter X has memorised three sections and knows nothing."}

    ;; -----------------------------------------------------------------------
    ;; Hat 3: Treasury as tax administrator. Where the EIN actually lives.

    {:statute/id            :irs/chapter
     :statute/topic         #{:tax}
     :statute/hat           :tax-administrator
     :statute/title         "26 CFR Chapter I -- Internal Revenue Service, Department of the Treasury"
     :statute/cfr-title     26
     :statute/cfr-node      [["chapter" "I"]]
     :statute/url           "https://www.ecfr.gov/current/title-26/chapter-I"
     :statute/verified-label "Internal Revenue Service, Department of the Treasury"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-26.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The IRS is a bureau of Treasury and its label says so. This is the title
      an operator building a `tax compliance service` will start in, and it is
      the title that does not contain the rules governing whether that operator
      may practise at all."}

    {:statute/id            :irs/identifying-numbers
     :statute/topic         #{:tax :identifier}
     :statute/hat           :tax-administrator
     :statute/title         "26 CFR 301.6109-1 -- Identifying numbers"
     :statute/cfr-title     26
     :statute/cfr-node      [["chapter" "I"] ["subchapter" "F"] ["part" "301"] ["subpart" "*"] ["subject_group" "*"] ["section" "301.6109-1"]]
     :statute/url           "https://www.ecfr.gov/current/title-26/chapter-I/subchapter-F/part-301/section-301.6109-1"
     :statute/verified-label "Identifying numbers."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-26.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "301"
     :statute/quote-section "301.6109-1"
     :statute/verified-quotes
     ["Employer identification numbers are used to identify employers."
      "that is required to furnish a taxpayer identifying number must use an employer identification number"]
     :statute/note
     "The EIN's stated purpose, in the regulation that creates it: to identify
      employers to the tax system. Not to establish eligibility for anything.
      The second span is the operative rule -- a non-individual that must
      furnish a taxpayer identifying number uses an EIN -- and note the
      condition it carries: `is required to furnish`. The requirement comes from
      some other rule about filing or withholding, never from this one. Both
      spans are needed: the first states the purpose, the second the trigger,
      and an adviser who has only the second will tell every new entity to get
      one."}

    {:statute/id            :irs/ein-definition
     :statute/topic         #{:tax :identifier}
     :statute/hat           :tax-administrator
     :statute/title         "26 CFR 301.7701-12 -- Employer identification number"
     :statute/cfr-title     26
     :statute/cfr-node      [["chapter" "I"] ["subchapter" "F"] ["part" "301"] ["subpart" "*"] ["section" "301.7701-12"]]
     :statute/url           "https://www.ecfr.gov/current/title-26/chapter-I/subchapter-F/part-301/section-301.7701-12"
     :statute/verified-label "Employer identification number."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-26.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "301"
     :statute/quote-section "301.7701-12"
     :statute/verified-quotes
     ["the term employer identification number means the taxpayer identifying number of an individual or other person (whether or not an employer)"]
     :statute/note
     "The parenthetical is the whole nuance and it is easy to lose: an EIN can
      belong to a person who employs nobody. The name describes the number's
      origin, not a fact about its holder. This is why `do I need an EIN` is
      never answered by asking whether the client has employees."}

    {:statute/id            :irs/employer-ein-application
     :statute/topic         #{:tax :identifier :registration}
     :statute/hat           :tax-administrator
     :statute/title         "26 CFR 31.6011(b)-1 -- Employers' identification numbers"
     :statute/cfr-title     26
     :statute/cfr-node      [["chapter" "I"] ["subchapter" "C"] ["part" "31"] ["subpart" "G"] ["section" "31.6011(b)-1"]]
     :statute/url           "https://www.ecfr.gov/current/title-26/chapter-I/subchapter-C/part-31/subpart-G/section-31.6011(b)-1"
     :statute/verified-label "Employers' identification numbers."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-26.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "31"
     :statute/quote-section "31.6011(b)-1"
     :statute/verified-quotes
     ["shall make an application on Form SS-4 for an identification number"]
     :statute/note
     "The `EIN issuance pathway` this leaf's blueprint sells, located: an
      employment-tax regulation requiring Form SS-4 from an employer paying
      FICA-taxable wages. The trigger is employment, not incorporation and not
      an intention to seek federal funding. Filing this form on a client's
      behalf is the act that 31 CFR 10.2 calls practice before the IRS."}

    ;; -----------------------------------------------------------------------
    ;; Hat 4: Treasury as practice regulator. This hat regulates the operator.

    {:statute/id            :treasury/secretary-subtitle
     :statute/topic         #{:organisation}
     :statute/hat           :practice-regulator
     :statute/title         "31 CFR Subtitle A -- Office of the Secretary of the Treasury"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "A"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-A"
     :statute/verified-label "Office of the Secretary of the Treasury"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "Where Circular 230 lives. The filing location is the finding: the rules
      about representing taxpayers are issued by the Secretary, not by the IRS,
      and so they sit in Money and Finance rather than Internal Revenue."}

    {:statute/id            :circular230/part
     :statute/topic         #{:practice :licensing}
     :statute/hat           :practice-regulator
     :statute/title         "31 CFR Part 10 -- Practice Before the Internal Revenue Service"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "A"] ["part" "10"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-A/part-10"
     :statute/verified-label "Practice Before the Internal Revenue Service"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "Circular 230. The single most important part in this catalog for the
      operator personally, and the one a title-26 reading never reaches."}

    {:statute/id            :circular230/scope
     :statute/topic         #{:practice :scope}
     :statute/hat           :practice-regulator
     :statute/title         "31 CFR 10.0 -- Scope of part"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "A"] ["part" "10"] ["section" "10.0"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-A/part-10/section-10.0"
     :statute/verified-label "Scope of part."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "10"
     :statute/quote-section "10.0"
     :statute/verified-quotes
     ["This part contains rules governing the recognition of attorneys, certified public accountants, enrolled agents, enrolled retirement plan agents, registered tax return preparers, and other persons representing taxpayers before the Internal Revenue Service."]
     :statute/note
     "The enumerated list is the eligibility gate. `Other persons representing
      taxpayers` does not open the door -- it extends the rules to people
      outside the named categories, it does not authorise them."}

    {:statute/id            :circular230/definition-of-practice
     :statute/topic         #{:practice :scope}
     :statute/hat           :practice-regulator
     :statute/title         "31 CFR 10.2 -- Definitions"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "A"] ["part" "10"] ["subpart" "A"] ["section" "10.2"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-A/part-10/subpart-A/section-10.2"
     :statute/verified-label "Definitions."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "10"
     :statute/quote-section "10.2"
     :statute/verified-quotes
     ["Practice before the Internal Revenue Service comprehends all matters connected with a presentation to the Internal Revenue Service"
      "preparing documents; filing documents; corresponding and communicating with the Internal Revenue Service"]
     :statute/note
     "The two spans together are what make this leaf's own business model a
      regulated activity. The first sets the breadth (`all matters connected
      with a presentation`), the second names the specific acts -- preparing,
      filing, corresponding -- that an EIN-application service performs for
      every client. Losing either span would leave the claim half-supported,
      which is why both are recorded and the offline suite pins the count."}

    {:statute/id            :circular230/who-may-practice
     :statute/topic         #{:practice :licensing}
     :statute/hat           :practice-regulator
     :statute/title         "31 CFR 10.3 -- Who may practice"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "A"] ["part" "10"] ["subpart" "A"] ["section" "10.3"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-A/part-10/subpart-A/section-10.3"
     :statute/verified-label "Who may practice."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "Heading-only on purpose. The section is long and enumerates each
      profession separately; pinning one span would suggest the list is shorter
      than it is. The operative question for an operator -- may I do this
      without being one of these -- is answered by 10.8, which is quoted."}

    {:statute/id            :circular230/preparers
     :statute/topic         #{:practice :licensing}
     :statute/hat           :practice-regulator
     :statute/title         "31 CFR 10.8 -- Return preparation and application of rules to other individuals"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "A"] ["part" "10"] ["subpart" "A"] ["section" "10.8"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-A/part-10/subpart-A/section-10.8"
     :statute/verified-label "Return preparation and application of rules to other individuals."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "10"
     :statute/quote-section "10.8"
     :statute/verified-quotes
     ["an individual must be an attorney, certified public accountant, enrolled agent, or registered tax return preparer to obtain a preparer tax identification number"]
     :statute/note
     "The credential chain in one sentence: paid preparation needs a PTIN, and a
      PTIN needs one of four statuses. An unenrolled operator who intends to
      charge for tax work needs to answer this before writing a line of
      software."}

    ;; -----------------------------------------------------------------------
    ;; Hat 5: Treasury as financial regulator. Binds people who never met it.

    {:statute/id            :ofac/chapter
     :statute/topic         #{:sanctions}
     :statute/hat           :financial-regulator
     :statute/title         "31 CFR Chapter V -- Office of Foreign Assets Control"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "B"] ["chapter" "V"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-B/chapter-V"
     :statute/verified-label "Office of Foreign Assets Control, Department of the Treasury"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "Forty-six parts, forty-five of them live sanctions programmes -- more than
      three times the whole DTAR. The bulk of Treasury's regulatory output is
      here, and none of it is about being a contractor or a grantee. It binds
      whoever transacts."}

    {:statute/id            :ofac/procedures
     :statute/topic         #{:sanctions}
     :statute/hat           :financial-regulator
     :statute/title         "31 CFR Part 501 -- Reporting, Procedures and Penalties Regulations"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "B"] ["chapter" "V"] ["part" "501"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-B/chapter-V/part-501"
     :statute/verified-label "Reporting, Procedures and Penalties Regulations"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"}

    {:statute/id            :ofac/records
     :statute/topic         #{:sanctions :recordkeeping}
     :statute/hat           :financial-regulator
     :statute/title         "31 CFR 501.601 -- Records and recordkeeping requirements"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "B"] ["chapter" "V"] ["part" "501"] ["subpart" "C"] ["section" "501.601"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-B/chapter-V/part-501/subpart-C/section-501.601"
     :statute/verified-label "Records and recordkeeping requirements."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "501"
     :statute/quote-section "501.601"
     :statute/verified-quotes
     ["every person engaging in any transaction subject to the provisions of this chapter shall keep a full and accurate record of each such transaction"
      "available for examination for at least 10 years after the date of such transaction"]
     :statute/note
     "`Every person` and `10 years`, both quoted because both are surprising.
      This is a Treasury recordkeeping duty owed by parties with no Treasury
      relationship, and its retention period is longer than the ordinary
      three-year federal tax assessment window a client will have been told to
      plan around. A records policy built from tax advice alone is short by
      seven years for anything sanctions-adjacent."}

    {:statute/id            :fincen/chapter
     :statute/topic         #{:aml}
     :statute/hat           :financial-regulator
     :statute/title         "31 CFR Chapter X -- Financial Crimes Enforcement Network"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "B"] ["chapter" "X"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-B/chapter-X"
     :statute/verified-label "Financial Crimes Enforcement Network, Department of the Treasury"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"}

    {:statute/id            :fincen/general
     :statute/topic         #{:aml}
     :statute/hat           :financial-regulator
     :statute/title         "31 CFR Part 1010 -- General Provisions"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "B"] ["chapter" "X"] ["part" "1010"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-B/chapter-X/part-1010"
     :statute/verified-label "General Provisions"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"}

    {:statute/id            :fincen/beneficial-ownership
     :statute/topic         #{:aml :registration}
     :statute/hat           :financial-regulator
     :statute/title         "31 CFR 1010.380 -- Reports of beneficial ownership information"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "B"] ["chapter" "X"] ["part" "1010"] ["subpart" "C"] ["section" "1010.380"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-B/chapter-X/part-1010/subpart-C/section-1010.380"
     :statute/verified-label "Reports of beneficial ownership information."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "1010"
     :statute/quote-section "1010.380"
     :statute/verified-quotes
     ["the term “reporting company” means: (i) [Reserved] (ii) Any entity that is:"
      "Formed under the law of a foreign country"]
     :statute/note
     "The highest-stakes live fact in this catalog, and the one most likely to
      be got wrong from memory. The domestic reporting company category is
      `[Reserved]` -- emptied, with the numbering left standing -- so as of the
      pinned snapshot the definition reaches only entities formed abroad and
      registered to do business in a State. An adviser working from 2024
      material will tell a domestic LLC it must file. That advice now causes an
      unnecessary disclosure of personal identifying information to a federal
      database. Both spans are recorded because the `[Reserved]` marker alone
      could survive a redefinition elsewhere in the section, and the foreign
      formation clause alone would not show that the domestic branch is gone.
      This entry is also the reason the catalog pins a snapshot date rather
      than tracking `current`: an entry whose value is that it is CURRENTLY
      counter-intuitive must be re-verified deliberately, not silently."}

    {:statute/id            :cfius/chapter
     :statute/topic         #{:investment-screening}
     :statute/hat           :financial-regulator
     :statute/title         "31 CFR Chapter VIII -- Office of Investment Security"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "B"] ["chapter" "VIII"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-B/chapter-VIII"
     :statute/verified-label "Office of Investment Security, Department of the Treasury"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "CFIUS. Recorded because it is the third Treasury regime that binds a firm
      through its transactions rather than its awards -- relevant to any client
      taking foreign investment, and invisible from a tax-registration framing."}

    {:statute/id            :fiscal/chapter
     :statute/topic         #{:payments}
     :statute/hat           :financial-regulator
     :statute/title         "31 CFR Chapter II -- Fiscal Service"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "B"] ["chapter" "II"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-B/chapter-II"
     :statute/verified-label "Fiscal Service, Department of the Treasury"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The bureau that actually disburses federal payments and collects federal
      debts. A client whose award is offset against an unrelated federal debt
      meets Treasury here, not at the awarding agency."}

    ;; -----------------------------------------------------------------------
    ;; Hat 6: Treasury outside the Treasury title.

    {:statute/id            :occ/chapter
     :statute/topic         #{:banking}
     :statute/hat           :elsewhere
     :statute/title         "12 CFR Chapter I -- Comptroller of the Currency"
     :statute/cfr-title     12
     :statute/cfr-node      [["chapter" "I"]]
     :statute/url           "https://www.ecfr.gov/current/title-12/chapter-I"
     :statute/verified-label "Comptroller of the Currency, Department of the Treasury"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-12.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "A Treasury bureau whose entire rulebook is in Banks and Banking. Recorded
      heading-only: no full-text endpoint is declared for title 12, so this
      entry deliberately carries no quote. It is here to make the count of
      titles honest -- Treasury's rules are in 2, 12, 26, 31 and 48 CFR, and a
      catalog that stopped at 31 would have claimed completeness it does not
      have."}

    ;; -----------------------------------------------------------------------
    ;; Hat 7: headings that name Treasury for functions it no longer runs.

    {:statute/id            :stale/secret-service
     :statute/topic         #{:organisation}
     :statute/hat           :stale-label
     :statute/title         "31 CFR Chapter IV -- Secret Service"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "B"] ["chapter" "IV"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-B/chapter-IV"
     :statute/verified-label "Secret Service, Department of the Treasury"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The label is recorded because it is what the CFR says, not because it is
      currently true of the agency. The chapter carries live, unreserved parts.
      Treat a CFR chapter heading as a filing location, never as an org chart:
      the heading is verified, the ownership it asserts is not something this
      catalog can verify from eCFR and is not claimed here."}

    {:statute/id            :stale/fletc
     :statute/topic         #{:organisation}
     :statute/hat           :stale-label
     :statute/title         "31 CFR Chapter VII -- Federal Law Enforcement Training Center"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "B"] ["chapter" "VII"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-B/chapter-VII"
     :statute/verified-label "Federal Law Enforcement Training Center, Department of the Treasury"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "Same pattern. Worth noting that the operative text under this heading --
      31 CFR 700.2, which defines where the rules apply -- names FLETC's sites
      and no department at all, so the chapter heading is the only
      organisational assertion in the chapter."}

    {:statute/id            :stale/atf-practice
     :statute/topic         #{:organisation :practice}
     :statute/hat           :stale-label
     :statute/title         "31 CFR Part 8 -- Practice Before the Bureau of Alcohol, Tobacco and Firearms"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "A"] ["part" "8"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-A/part-8"
     :statute/verified-label "Practice Before the Bureau of Alcohol, Tobacco and Firearms"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The most useful of the three for an operator, because it sits directly
      beside part 10 and has the same shape -- `Practice Before ...` -- under
      the same subtitle. A reader scanning subtitle A for practice rules finds
      two parts and must know which of the two named bureaus still exists under
      that name to know which one governs. As with the other two entries under
      this hat, the heading is recorded because it is what the CFR says: it is
      a filing location, not an org chart, and the bureau it names is not
      something this catalog verifies from eCFR."}
    ]})

;; ---------------------------------------------------------------------------

(def absences
  "Things a competent reader expects to find for this department, which are not
  there -- each recorded so the live gate can confirm they are STILL not there.

  Two of these are TEXT absences rather than label absences: the claim is about
  words inside a regulation's running text, not about a heading. Those carry a
  `:absence/control-text` pattern that MUST match in the very same fetched
  document, because a text absence is the easiest kind of check to pass for
  free -- fetch nothing, match nothing, report confirmed."
  [{:absence/id :treasury/no-taxpayer-identifier-in-assistance-rules
    :absence/claim
    "The words `taxpayer`, `TIN`, `EIN` and `employer identification` do not
     appear anywhere in the text of 2 CFR part 25 -- the part whose entire
     subject is the identifier a federal financial assistance applicant must
     hold. The identifier that part names is the UEI, and the system it names
     is SAM.gov. This is the checked negative underneath this leaf's central
     correction: an EIN is not a credential for federal assistance, and the
     rule that decides the question never mentions one. The same scan over
     2 CFR part 200 -- the whole of OMB's uniform guidance -- also returns
     nothing, so this is not an artefact of looking at too small a part."
    :absence/absent-text
    {:statute/cfr-title 2
     :statute/part      "25"
     :statute/pattern   "(?i)taxpayer|employer identification|\\bTIN\\b|\\bEIN\\b"}
    :absence/control-text
    {:statute/pattern "(?i)unique entity identifier"
     :absence/control-note
     "Part 25's own subject. If this stops matching, the fetch returned
      something that is not part 25 -- or nothing at all -- and the absence
      above proves nothing. Measured at 6 occurrences when recorded."}
    :absence/see-instead
    {:statute/id            :uei/purpose-see-instead
     :statute/title         "2 CFR 25.100 -- Purpose of this part"
     :statute/cfr-title     2
     :statute/cfr-node      [["subtitle" "A"] ["chapter" "I"] ["part" "25"] ["subpart" "A"] ["section" "25.100"]]
     :statute/url           "https://www.ecfr.gov/current/title-2/subtitle-A/chapter-I/part-25/subpart-A/section-25.100"
     :statute/verified-label "Purpose of this part."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-2.json"
     :statute/verified-at   "2026-08-20"}}

   {:absence/id :treasury/no-practice-rules-in-title-26
    :absence/claim
    "The phrase `practice before` appears in no node label anywhere in title 26.
     The rules governing who may represent a taxpayer before the IRS -- and
     therefore whether this leaf's own operator may file a client's Form SS-4
     for a fee -- are not in the Internal Revenue title at all. They are 31 CFR
     part 10, Circular 230, issued by the Secretary and filed under Money and
     Finance. An operator who scoped their compliance reading to `the tax title`
     has read around their own licensing question."
    :absence/absent-label
    {:statute/cfr-title 26
     :statute/under     []
     :statute/pattern   "(?i)practice before"}
    :absence/control-label
    {:statute/pattern "(?i)internal revenue"
     :absence/control-note
     "Title 26 names the Internal Revenue Service in many labels, starting with
      chapter I itself. If this control stops matching, the scan is not reading
      title 26 and the absence is vacuous. Measured at 50 matching labels when
      recorded."}
    :absence/see-instead
    {:statute/id            :circular230/part-see-instead
     :statute/title         "31 CFR Part 10 -- Practice Before the Internal Revenue Service"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "A"] ["part" "10"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-A/part-10"
     :statute/verified-label "Practice Before the Internal Revenue Service"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"}}

   {:absence/id :treasury/no-dtar-administrative-matters-part
    :absence/claim
    "48 CFR chapter 10 contains no part 1004. Treasury has not supplemented FAR
     part 4 -- Administrative Matters -- which is where the FAR's own taxpayer
     identification subpart 4.9 lives. DoD supplemented it at part 204 and DOT
     at part 1204; Treasury did not. Combined with 48 CFR 1001.101 (`When FAR
     coverage is adequate, there will be no corresponding DTAR coverage`), the
     gap is a positive statement rather than an ambiguity: a firm contracting
     with Treasury handles taxpayer identification under the unmodified FAR.
     There is likewise no part 1025, so the same is true of foreign acquisition
     and the Buy American rules."
    :absence/absent-part
    {:statute/cfr-title 48
     :statute/under     [["chapter" "10"]]
     :statute/part      "1004"}
    :absence/control-part
    {:statute/part "1019"
     :absence/control-note
     "Part 1019 (Small Business Programs) is in the same chapter. If it stops
      being found, the walk reached the wrong subtree or an empty one, and
      `no part 1004` was confirmed against nothing."}
    :absence/see-instead
    {:statute/id            :far/taxpayer-identification-see-instead
     :statute/title         "48 CFR Subpart 4.9 -- Taxpayer Identification Number Information"
     :statute/cfr-title     48
     :statute/cfr-node      [["chapter" "1"] ["subchapter" "A"] ["part" "4"] ["subpart" "4.9"]]
     :statute/url           "https://www.ecfr.gov/current/title-48/chapter-1/subchapter-A/part-4/subpart-4.9"
     :statute/verified-label "Taxpayer Identification Number Information"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"
     :statute/verified-at   "2026-08-20"}}

   {:absence/id :treasury/no-domestic-reporting-company
    :absence/claim
    "The phrase `domestic reporting company` does not appear in 31 CFR
     1010.380. The domestic branch of the reporting company definition is
     `[Reserved]`, and what remains reaches only entities formed under foreign
     law and registered to do business in a State. Recorded as an absence in
     ADDITION to the quoted spans on the entry itself, because the two fail in
     different ways: a redefinition that reintroduces domestic companies under
     a new heading would break this absence while the `[Reserved]` quote might
     survive, and a renumbering would break the quote while leaving the phrase
     absent. The claim this leaf gives clients is high-stakes enough to be
     worth checking from both directions."
    :absence/absent-text
    {:statute/cfr-title 31
     :statute/part      "1010"
     :statute/section   "1010.380"
     :statute/pattern   "(?i)domestic reporting company"}
    :absence/control-text
    {:statute/pattern "(?i)formed under the law of a foreign country"
     :absence/control-note
     "The surviving branch of the same definition. If this stops matching, the
      section was renumbered or the fetch failed, and the absence above is
      vacuous rather than reassuring."}
    :absence/see-instead
    {:statute/id            :fincen/beneficial-ownership-see-instead
     :statute/title         "31 CFR 1010.380 -- Reports of beneficial ownership information"
     :statute/cfr-title     31
     :statute/cfr-node      [["subtitle" "B"] ["chapter" "X"] ["part" "1010"] ["subpart" "C"] ["section" "1010.380"]]
     :statute/url           "https://www.ecfr.gov/current/title-31/subtitle-B/chapter-X/part-1010/subpart-C/section-1010.380"
     :statute/verified-label "Reports of beneficial ownership information."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-31.json"
     :statute/verified-at   "2026-08-20"}}])

;; ---------------------------------------------------------------------------
;; Accessors.

(defn entries
  "Every catalog entry, flattened across ISO keys."
  []
  (vec (mapcat val catalog)))

(defn by-hat
  "Entries wearing `hat` -- the question `which Treasury am I dealing with`."
  [hat]
  (filterv #(= hat (:statute/hat %)) (entries)))

(defn by-topic
  "Entries tagged with `topic`."
  [topic]
  (filterv #(contains? (:statute/topic %) topic) (entries)))

(defn titles-covered
  "Sorted CFR titles this catalog cites. Five, which is the finding."
  []
  (vec (sort (distinct (map :statute/cfr-title (entries))))))

(defn quoted-entries
  "Entries carrying at least one byte-exact span of live regulation text."
  []
  (filterv #(seq (:statute/verified-quotes %)) (entries)))

(defn quote-count
  "Total spans the live gate must find. Shrinking this without noticing is the
  failure mode the offline suite exists to prevent."
  []
  (reduce + 0 (map #(count (:statute/verified-quotes %)) (entries))))

(defn summary
  "One line per entry -- for humans reading the catalog at a terminal."
  []
  (str/join "\n"
            (for [e (entries)]
              (str (name (:statute/hat e)) "\t" (:statute/title e)))))
