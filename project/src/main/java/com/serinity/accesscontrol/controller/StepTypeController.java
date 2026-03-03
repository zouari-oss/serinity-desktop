package com.serinity.accesscontrol.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;

public class StepTypeController {
  @FXML
  private ToggleButton btnMoment;
  @FXML
  private ToggleButton btnDay;

  private StateOfMindWizardController wizard;

  public void setWizard(final StateOfMindWizardController wizard) {
    this.wizard = wizard;
    // if selected enable next
    if (wizard != null) {
      wizard.setCanGoNext(btnMoment.isSelected() || btnDay.isSelected());
    }
  }

  public String getSelectedType() {
    if (btnMoment.isSelected())
      return "MOMENT";
    if (btnDay.isSelected())
      return "DAY";
    return null;
  }

  // PREFILL (for Edit)
  public void setSelectedType(final String type) {
    if (type == null) {
      btnMoment.setSelected(false);
      btnDay.setSelected(false);
    } else if ("MOMENT".equalsIgnoreCase(type)) {
      btnMoment.setSelected(true);
      btnDay.setSelected(false);
    } else if ("DAY".equalsIgnoreCase(type)) {
      btnDay.setSelected(true);
      btnMoment.setSelected(false);
    } else {
      btnMoment.setSelected(false);
      btnDay.setSelected(false);
    }

    if (wizard != null) {
      wizard.setCanGoNext(btnMoment.isSelected() || btnDay.isSelected());
    }
  }

  @FXML
  private void onSelect() {
    if (btnMoment.isSelected())
      btnDay.setSelected(false);
    if (btnDay.isSelected())
      btnMoment.setSelected(false);

    if (wizard != null) {
      wizard.setCanGoNext(btnMoment.isSelected() || btnDay.isSelected());
    }
  }
}
