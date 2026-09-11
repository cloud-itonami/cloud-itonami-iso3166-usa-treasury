# cloud-itonami-iso3166-usa-treasury

Open ISO 3166 **agency-level** Blueprint for **USA-TREASURY**: Department of the
Treasury (parent country: **USA**).

This leaf designs a forkable OSS business for an independent operator
navigating **Department of the Treasury**-specific procurement, assistance and
regulatory compliance, composing with the country coordinator
`cloud-itonami-iso3166-usa`.

## What this is NOT

- **Not the Department of the Treasury, and not the IRS.** Commercial
  compliance navigation only.
- **Not legal or tax advice.** Cite official sources; route licensed work to
  counsel. See the licensing caveat below — for this leaf that warning is not
  boilerplate.

## The verified catalog

`src/statute/facts.cljk` is the spec-basis: **40 regulatory anchors across five
CFR titles (2, 12, 26, 31, 48), 18 byte-exact quotes of live regulation text,
and 4 checked absences.** Every heading is the byte-exact `label_description`
returned by the official eCFR versioner API, and every quote is a byte-exact
span of the section text returned by the same API, both pinned to the
`2026-08-18` snapshot.

```bash
nbb tools/verify_citations.cljk     # live gate: re-fetches eCFR, exits 0/1/2
clojure -M:test                     # offline invariants (24 tests)
clojure -M:lint
```

The live gate distinguishes three outcomes on purpose:

| exit | meaning |
|---|---|
| 0 | answered; every heading, quote and absence checked out, and every floor met |
| 1 | answered; something has drifted — the message names which entry and how |
| 2 | **could not answer** — network failure, an undeclared endpoint, a control that stopped matching, or fewer checks than the floor |

`Nothing was checked` and `nothing was wrong` must not share an exit code, so a
vacuous run can never be read as a pass. Each of the four absences carries a
**control** — a pattern or part that MUST be found in the same fetched document
or subtree. Without one, fetching an empty document confirms every claim of the
form *these words do not appear*.

## Where this contradicts the blueprint

`blueprint.edn` describes the product as an *EIN issuance pathway and federal
tax-registration checklist for award eligibility*. Building the catalog
refuted two independent halves of that sentence, and the refutations are
recorded as data — a quoted span or a checked negative — rather than as prose:

1. **Award eligibility does not run on the EIN.** 2 CFR 25.100(a) makes the
   unique entity identifier *"the universal identifier for Federal financial
   assistance applicants"*, with SAM.gov as the repository. Scanning the whole
   text of 2 CFR part 25 — the part whose subject **is** the identifier an
   applicant must hold — for `taxpayer`, `TIN`, `EIN` or
   `employer identification` returns nothing. The same scan over 2 CFR part 200,
   the entirety of OMB's uniform guidance, also returns nothing.

2. **Obtaining an EIN for a client is itself practice before the IRS.** 31 CFR
   10.2 defines that practice to comprehend *"all matters connected with a
   presentation to the Internal Revenue Service"*, expressly including
   *"preparing documents; filing documents; corresponding and communicating
   with the Internal Revenue Service"*. 31 CFR part 10 — Circular 230 — then
   restricts who may do it, and 10.8 ties paid preparation to a PTIN available
   only to attorneys, CPAs, enrolled agents and registered tax return
   preparers. **The phrase `practice before` appears in no node label anywhere
   in title 26**, so a compliance reading scoped to the tax title never reaches
   the operator's own licensing question.

The counterweight, which keeps this from over-correcting into *the EIN never
matters*: on the **contract** side the taxpayer identifier is real — FAR
subpart 4.9 is titled *Taxpayer Identification Number Information* and 48 CFR
52.204-3 collects it. So the first question for any Treasury dollar is **which
side of the money is this**, and the answer changes which identifier even
exists as a concept. That question, not the SS-4, is what an operator can
actually sell.

`blueprint.edn` is left unchanged pending an owner decision, because fleet
consumers key on `:itonami.blueprint/name`.

## Five hats, and the two that surprise people

The catalog tags every entry with a `:statute/hat`, because conflating them is
the failure it exists to prevent:

| hat | Treasury as | where |
|---|---|---|
| `:acquirer` | buyer, under the FAR as supplemented by the DTAR | 48 CFR ch. 10 |
| `:grantor` | awarder of assistance | 2 CFR ch. X |
| `:tax-administrator` | the IRS | 26 CFR ch. I |
| `:practice-regulator` | **regulator of the operator** | 31 CFR part 10 |
| `:financial-regulator` | **regulator of everyone** | 31 CFR ch. V, VIII, X |

The last two are the ones a procurement-shaped reading misses entirely. OFAC's
31 CFR 501.601 obliges *"every person"* engaging in a covered transaction to
keep records *"for at least 10 years"* — a Treasury duty owed by parties who
are neither contractors nor grantees, with a retention period far longer than
the ordinary federal tax planning horizon.

Two structural hazards are carried as entries rather than absences:

- **A chapter heading is not a statement of current ownership.** 31 CFR chapter
  IV is still labelled *Secret Service, Department of the Treasury*, chapter
  VII *Federal Law Enforcement Training Center, Department of the Treasury*,
  and part 8 *Practice Before the Bureau of Alcohol, Tobacco and Firearms* —
  all with live, unreserved text. The labels are recorded byte-exactly under a
  `:stale-label` hat because that is what the CFR says; the ownership they
  assert is not something this catalog verifies, and is not claimed.
- **Treasury's reach is not confined to 31 CFR.** 12 CFR chapter I is
  *Comptroller of the Currency, Department of the Treasury* — a Treasury bureau
  whose entire rulebook sits in Banks and Banking.

## One live fact worth re-checking before advising anyone

31 CFR 1010.380(c)(1)(i) — the **domestic** reporting company branch of
FinCEN's beneficial-ownership rule — is `[Reserved]`. As of the pinned
snapshot the definition reaches only entities *"Formed under the law of a
foreign country"* and registered to do business in a State. Advice written
from 2024 material tells every domestic LLC to file, which now causes an
unnecessary disclosure of personal identifying information to a federal
database. This is checked from two directions — a quoted span **and** an
absence — because a redefinition and a renumbering would break different ones.

This entry is also why the catalog pins a dated snapshot rather than tracking
`current`: an entry whose value is that it is *currently* counter-intuitive
must be re-verified deliberately, not silently.

## Official surface

- Department of the Treasury — https://home.treasury.gov/
- Internal Revenue Service — https://www.irs.gov/
- eCFR (the machine-verified source) — https://www.ecfr.gov/

## Capability layer

Resolves via `kotoba-lang/iso3166` (`USA-TREASURY`, parent `USA`).

## License

AGPL-3.0-or-later.
