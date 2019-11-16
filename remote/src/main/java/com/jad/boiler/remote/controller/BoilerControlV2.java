package com.jad.boiler.remote.controller;

import com.jad.boiler.remote.dto.v1.Action;
import com.jad.boiler.remote.dto.v1.Info;
import com.jad.boiler.remote.dto.v1.Status;
import com.jad.boiler.remote.service.HistoryService;
import com.jad.boiler.remote.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RequestMapping({"/boiler/v2/"})
@RestController
public class BoilerControlV2 {
   @Autowired
   private StatusService statusService;
   @Autowired
   private HistoryService historyService;



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

   @GetMapping("resetError")
   public void resetHistoryError() {
      historyService.resetError();
   }
}
