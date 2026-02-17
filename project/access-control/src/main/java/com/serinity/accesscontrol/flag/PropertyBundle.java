package com.serinity.accesscontrol.flag;

public enum PropertyBundle {
  SUPPORTED_LANGUAGES_BUNDLE("bundles.supported-languages"),
  DEFAULT_MESSAGES_BUNDLE("bundles.messages"),
  ENGLISH_MESSAGES_BUNDLE("bundles.messages"),
  FRENCH_MESSAGES_BUNDLE("bundles.messages_fr");

  private final String baseName;

  PropertyBundle(final String baseName) {
    this.baseName = baseName;
  }

  public final String getBaseName() {
    return baseName;
  }
} // PropertyBundle enum
