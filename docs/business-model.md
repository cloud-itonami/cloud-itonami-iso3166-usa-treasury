# Business Model: Independent Treasury/IRS EIN & Federal Tax Compliance Service — United States

## Classification

- Repository: `cloud-itonami-iso3166-usa-treasury`
- ISO 3166 (agency-level): `USA-TREASURY`, parent `USA`
- Ooyake cross-reference: `gov.usa.treasury` (Department of the Treasury)
- Activity: EIN issuance pathway and federal tax-registration checklist for award eligibility

## Customer

- an operator already using `cloud-itonami-iso3166-usa` whose contract
  touches Department of the Treasury rules or buying channels
- a foreign SME entering a Department of the Treasury-specific public program for the first time

## Offer

- walkthrough and evidence checklist for: EIN issuance pathway and federal tax-registration checklist for award eligibility
- ongoing regulatory-change monitoring for this body's public sources
- compliance-audit export package

## Trust Controls

- `:filing/submit` never auto-commits at any phase
- fabricated regulatory claims are HARD holds
- not legal advice — cite https://www.irs.gov/

## Boundary

- **`cloud-itonami-iso3166-usa`**: country coordinator (general U.S. market entry)
- **`com-etzhayyim-ooyake`**: read-only civic atlas (never acts as the body)
