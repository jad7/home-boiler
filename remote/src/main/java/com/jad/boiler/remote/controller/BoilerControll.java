package com.jad.boiler.remote.controller;

import com.jad.boiler.remote.dto.Action;
import com.jad.boiler.remote.dto.Info;
import com.jad.boiler.remote.dto.Status;
import com.jad.boiler.remote.service.StatusService;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping({"/boiler/v1/"})
@RestController
public class BoilerControll {
   @Autowired
   private StatusService statusService;

   public BoilerControll() {
   }

   @GetMapping({"info"})
   public Info getInfo() {
      return this.statusService.getInfo();
   }

   @PostMapping({"action"})
   public void applyAction(@RequestBody List<Action> actions) {
      this.statusService.addActions(actions);
   }

   @PostMapping({"setStatus"})
   public Collection<Action> setStatus(@RequestBody Status currentStatus) {
      return this.statusService.setState(currentStatus);
   }
}
