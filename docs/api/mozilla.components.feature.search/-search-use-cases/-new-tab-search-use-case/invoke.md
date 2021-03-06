[android-components](../../../index.md) / [mozilla.components.feature.search](../../index.md) / [SearchUseCases](../index.md) / [NewTabSearchUseCase](index.md) / [invoke](./invoke.md)

# invoke

`fun invoke(searchTerms: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) [(source)](https://github.com/mozilla-mobile/android-components/blob/master/components/feature/search/src/main/java/mozilla/components/feature/search/SearchUseCases.kt#L67)

Overrides [SearchUseCase.invoke](../-search-use-case/invoke.md)

`fun invoke(searchTerms: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, source: `[`Source`](../../../mozilla.components.browser.session/-session/-source/index.md)`, selected: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, private: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) [(source)](https://github.com/mozilla-mobile/android-components/blob/master/components/feature/search/src/main/java/mozilla/components/feature/search/SearchUseCases.kt#L79)

Triggers a search on a new session, using the default search engine for the provided search terms.

### Parameters

`searchTerms` - the search terms.

`selected` - whether or not the new session should be selected, defaults to true.

`private` - whether or not the new session should be private, defaults to false.

`source` - the source of the new session.