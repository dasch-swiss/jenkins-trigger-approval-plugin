# Jenkins Trigger Approval Plugin

This Jenkins plugin adds a trigger approval mechanism. When enabled, all triggers such as e.g. `UserIdCause` or `SCMTriggerCause` are by default blocked. When a trigger is blocked it is added to a pending list which users with `CONFIGURE` permission can approve, deny or ignore.  
This plugin is mostly intended for test environments where triggers should not run on their own. It is not intended for security or authorization purposes.

### Usage
Whenever a new trigger is encountered that is not already matched by the approve, deny or ignore list, it is added to the pending list.

Triggers can be approved, denied or ignored from the Trigger Approval page (Manage > Trigger Approval).
There are three options when approving or denying a trigger:
- For a specific number of times:  
  How often a approve or deny rule will be applied before it expires and is removed automatically.
- For a specific job:  
  Whether the approve or deny rule should apply only to a specific job (task URL is matched by a regex).
- For exactly this cause:  
  Whether the rule should only apply to exactly the same cause. The cause's description and hash is shown in the table. For example, for a `UserIdCause` the rule would only be applied if the same user triggered it. Note that the equality semantics are implementation specific and not all causes may implement this properly.

When a trigger is approved, denied or ignored it is automatically removed from the pending list.

The deny list takes precedence over the approve list. For example, if you approve `UserIdCause` for any job and then deny `UserIdCause` for a specific job, it will be denied only for that specific job and be approved for all other jobs.

The ignore list has no effect at all on approval or denial of triggers. Ignore list entries will always remain in the ignore list, even when they're approved or denied, and can be reused. They can be removed manually from the list.

### Settings

All settings are found in the Trigger Approval section under Manage > Configure System.

|Setting|Use|
|-|-|
|Enable|Whether the trigger approval mechanism should be enabled. Default: `false`.|
|Allow jobs without causes|Whether jobs without any causes attached should be allowed to run. Required e.g. for pipeline jobs to work at all. Default: `true`.|
|Enable strict checking|If enabled all causes of a job causes must be approved. Otherwise just one cause being approved is sufficient. A job can end up with multiple causes if it is stuck in the queue for a while for example. Default: `false`.|
|Maximum number of pending causes|Maximum number of entries the pending list keeps track of. Default: `10`.|
|Enable logging|Whether the plugin should write a log entry when a trigger is blocked. Default: `true`.|

### Development
Starting a development Jenkins instance with this plugin: `mvn hpi:run`

Building the plugin: `mvn package`
